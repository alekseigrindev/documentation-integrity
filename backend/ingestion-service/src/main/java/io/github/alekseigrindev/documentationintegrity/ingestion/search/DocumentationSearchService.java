package io.github.alekseigrindev.documentationintegrity.ingestion.search;

import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentChunkRepository;
import io.github.alekseigrindev.documentationintegrity.ingestion.embedding.TextEmbeddingModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;

/**
 * Runs full-text search and assembles cited results.
 */
@Service
@RequiredArgsConstructor
public class DocumentationSearchService {

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

    private List<DocumentationSearchHit> getByQuery(
            String query,
            RetrievalMethod retrievalMethod
    ) {
        return switch (retrievalMethod) {
            case LEXICAL -> lexicalSearchByQuery(query);
            case VECTOR -> vectorSearchByQuery(query);
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
                ? List.of(RetrievalMethod.LEXICAL, RetrievalMethod.VECTOR)
                : List.of(RetrievalMethod.LEXICAL);
    }
}
