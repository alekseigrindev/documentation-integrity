package io.github.alekseigrindev.documentationintegrity.ingestion.search;

import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentChunkRepository;
import io.github.alekseigrindev.documentationintegrity.ingestion.embedding.TextEmbeddingModel;
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
    private static final int RESULT_LIMIT = 10;

    private final DocumentChunkRepository documentChunkRepository;
    private final Optional<TextEmbeddingModel> textEmbeddingModel;

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
            List<DocumentationSearchHit> vectorSearchResults
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
                        lexicalSearchResults.stream().limit(RESULT_LIMIT),
                        vectorSearchResults.stream().limit(RESULT_LIMIT)
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
                .limit(RESULT_LIMIT)
                .map(entry -> hitsByChunkId.get(entry.getKey()))
                .toList();
    }

    private Map<UUID, Double> calculateChunksScores(List<DocumentationSearchHit> rawHits) {
        int resultCount = Math.min(rawHits.size(), RESULT_LIMIT);

        return IntStream.range(0, resultCount)
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
                    vectorSearchByQuery(query),
                    lexicalSearchByQuery(query)
            );
        };
    }

    private List<DocumentationSearchHit> vectorSearchByQuery(String query) {
        float[] queryEmbedding = embedQuery(query);

        return toSearchHits(
                documentChunkRepository.searchCitableChunksByEmbedding(
                        queryEmbedding
                )
        );
    }

    private List<DocumentationSearchHit> lexicalSearchByQuery(String query) {
        return toSearchHits(
                documentChunkRepository.searchCitableChunksByQuery(query)
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
                    vectorSearchByQueryAndSourceIds(query, sourceIds),
                    lexicalSearchByQueryAndSourceIds(query, sourceIds)
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
                                sourceIds
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
                                sourceIds
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
        return textEmbeddingModel.isPresent()
                ? List.of(
                        RetrievalMethod.LEXICAL,
                        RetrievalMethod.VECTOR,
                        RetrievalMethod.HYBRID
                        )
                : List.of(RetrievalMethod.LEXICAL);
    }
}
