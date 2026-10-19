package io.github.alekseigrindev.documentationintegrity.ingestion.search;

import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentChunkRepository;
import io.github.alekseigrindev.documentationintegrity.ingestion.embedding.TextEmbeddingModel;
import io.github.alekseigrindev.documentationintegrity.ingestion.reranking.TextRerankingModel;
import io.github.alekseigrindev.documentationintegrity.ingestion.reranking.RerankingInput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Runs full-text search and assembles cited results.
 */
@Service
@RequiredArgsConstructor
public class DocumentationSearchService {

    private static final int RRF_RANK_CONSTANT = 60;

    private final DocumentChunkRepository documentChunkRepository;
    private final Optional<TextEmbeddingModel> textEmbeddingModel;
    private final Optional<TextRerankingModel> textRerankingModel;

    private final RetrievalProperties retrievalProperties;

    public List<DocumentationSearchHit> search(
            String query,
            Set<UUID> sourceIds,
            RetrievalMethod retrievalMethod) {
        return sourceIds.isEmpty()
                ? getByQuery(query, retrievalMethod)
                : getByQueryAndSourceIds(query, sourceIds, retrievalMethod);
    }

    private List<DocumentationSearchHit> fuseRankedResults(
            List<DocumentationSearchHit> lexicalSearchResults,
            List<DocumentationSearchHit> vectorSearchResults,
            int outputLimit
    ) {
        Map<UUID, Double> lexicalScores = calculateChunksScores(lexicalSearchResults);
        Map<UUID, Double> vectorScores = calculateChunksScores(vectorSearchResults);

        final Map<UUID, Double> combinedScores = new HashMap<>(lexicalScores);

        vectorScores.forEach((chunkId, score) ->
                combinedScores.merge(
                        chunkId,
                        score,
                        Double::sum
                ));

        Map<UUID, DocumentationSearchHit> hitsByChunkId =
                Stream.concat(
                        lexicalSearchResults.stream().limit(retrievalProperties.lexicalCandidateLimit()),
                        vectorSearchResults.stream().limit(retrievalProperties.vectorCandidateLimit())
                ).collect(Collectors.toMap(
                        DocumentationSearchHit::chunkId,
                        Function.identity(),
                        (existingHit, duplicateHit) -> existingHit
                ));

        return combinedScores.entrySet().stream()
                .sorted(
                        Map.Entry.<UUID, Double>comparingByValue()
                                .reversed()
                                .thenComparing(Map.Entry.comparingByKey())
                )
                .limit(outputLimit)
                .map(entry -> hitsByChunkId.get(entry.getKey()))
                .toList();
    }

    private Map<UUID, Double> calculateChunksScores(List<DocumentationSearchHit> rawHits) {

        return IntStream.range(0, rawHits.size())
                .boxed()
                .collect(Collectors.toMap(
                        index -> rawHits.get(index).chunkId(),
                        index -> {
                            int rank = index + 1;
                            return 1.0 / (RRF_RANK_CONSTANT + rank);
                        },
                        Double::sum
                ));

    }

    private List<DocumentationSearchHit> getByQuery(
            String query,
            RetrievalMethod retrievalMethod
    ) {
        return switch (retrievalMethod) {
            case LEXICAL -> lexicalSearchByQuery(query);
            case VECTOR -> vectorSearchByQuery(query);
            case HYBRID -> fuseRankedResults(
                    lexicalSearchByQuery(query),
                    vectorSearchByQuery(query),
                    10
            );
            case HYBRID_RERANKED -> rerank(
                    query,
                    fuseRankedResults(
                            lexicalSearchByQuery(query),
                            vectorSearchByQuery(query),
                            retrievalProperties.rerankingCandidateLimit()
                    )
            );
        };
    }

    private List<DocumentationSearchHit> vectorSearchByQuery(
            String query
    ) {
        float[] queryEmbedding = embedQuery(query);

        return toSearchHits(
                documentChunkRepository.searchCitableChunksByEmbedding(
                        queryEmbedding,
                        retrievalProperties.vectorCandidateLimit()
                )
        );
    }

    private List<DocumentationSearchHit> lexicalSearchByQuery(String query) {
        return toSearchHits(
                documentChunkRepository.searchCitableChunksByQuery(query, retrievalProperties.lexicalCandidateLimit())
        );
    }

    private List<DocumentationSearchHit> getByQueryAndSourceIds(
            String query,
            Set<UUID> sourceIds,
            RetrievalMethod retrievalMethod
    ) {
        return switch (retrievalMethod) {
            case LEXICAL -> lexicalSearchByQueryAndSourceIds(query, sourceIds);
            case VECTOR -> vectorSearchByQueryAndSourceIds(query, sourceIds);
            case HYBRID -> fuseRankedResults(
                    lexicalSearchByQueryAndSourceIds(query, sourceIds),
                    vectorSearchByQueryAndSourceIds(query, sourceIds),
                    10
            );
            case HYBRID_RERANKED -> rerank(
                    query,
                    fuseRankedResults(
                            lexicalSearchByQueryAndSourceIds(query, sourceIds),
                            vectorSearchByQueryAndSourceIds(query, sourceIds),
                            retrievalProperties.rerankingCandidateLimit()
                    )
            );
        };
    }

    private List<DocumentationSearchHit> vectorSearchByQueryAndSourceIds(
            String query,
            Set<UUID> sourceIds
    ) {
        float[] queryEmbedding = embedQuery(query);

        return toSearchHits(
                documentChunkRepository
                        .searchCitableChunksByEmbeddingAndSourceIds(
                                queryEmbedding,
                                sourceIds,
                                retrievalProperties.vectorCandidateLimit()
                        )
        );
    }

    private List<DocumentationSearchHit> lexicalSearchByQueryAndSourceIds(
            String query,
            Set<UUID> sourceIds
    ) {
        return toSearchHits(
                documentChunkRepository
                        .searchCitableChunksByQueryAndSourceIds(
                                query,
                                sourceIds,
                                retrievalProperties.lexicalCandidateLimit()
                        )
        );
    }

    private float[] embedQuery(String query) {
        return textEmbeddingModel.orElseThrow(
                () -> new IllegalStateException(
                        "Vector retrieval is unavailable because "
                                + "the embedding model is disabled"
                )
        ).embedQuery(query);
    }

    private List<DocumentationSearchHit> toSearchHits(
            List<DocumentChunkRepository.CitableChunkSearchRow> rows
    ) {
        return rows.stream()
                .map(row -> new DocumentationSearchHit(
                        row.getChunkId(),
                        row.getChunkOrdinal(),
                        row.getContent(),
                        row.getChunkContentHash(),
                        row.getSourceId(),
                        row.getSourceLocator(),
                        row.getCanonicalUrl() == null
                                ? null
                                : URI.create(row.getCanonicalUrl()),
                        row.getProductVariant(),
                        row.getUpstreamVersion(),
                        row.getMediaType(),
                        row.getAcquiredAt(),
                        row.getDocumentContentHash(),
                        row.getAttribution()
                ))
                .toList();
    }

    public List<RetrievalMethod> getRetrievalMethods() {
        List<RetrievalMethod> methods = new ArrayList<>();
        methods.add(RetrievalMethod.LEXICAL);

        if (textEmbeddingModel.isPresent()) {
            methods.add(RetrievalMethod.VECTOR);
            methods.add(RetrievalMethod.HYBRID);

            if (textRerankingModel.isPresent()) {
                methods.add(RetrievalMethod.HYBRID_RERANKED);
            }
        }

        return methods;
    }

    private List<DocumentationSearchHit> rerank(
            String query,
            List<DocumentationSearchHit> candidates
    ) {
        TextRerankingModel model = textRerankingModel.orElseThrow(
                () -> new IllegalStateException(
                        "Reranking is unavailable because the model is disabled"
                )
        );

        if (candidates.isEmpty()) {
            return List.of();
        }

        List<RerankingInput> inputs = candidates.stream()
                .map(hit -> new RerankingInput(query, hit.content()))
                .toList();

        float[] scores = model.score(inputs);

        if (scores.length != candidates.size()) {
            throw new IllegalStateException(
                    "Reranker score count does not match candidate count"
            );
        }

        return IntStream.range(0, candidates.size())
                .boxed()
                .sorted(
                        Comparator.comparingDouble(
                                        (Integer index) -> scores[index]
                                ).reversed()
                                .thenComparingInt(Integer::intValue)
                )
                .limit(retrievalProperties.rerankingResultLimit())
                .map(candidates::get)
                .toList();

    }
}
