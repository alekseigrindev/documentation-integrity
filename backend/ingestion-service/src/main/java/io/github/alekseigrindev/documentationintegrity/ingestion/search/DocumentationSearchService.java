package io.github.alekseigrindev.documentationintegrity.ingestion.search;

import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Runs full-text search and assembles cited results.
 */
@Service
@RequiredArgsConstructor
public class DocumentationSearchService {

    private final DocumentChunkRepository documentChunkRepository;

    public List<DocumentationSearchHit> search(String query, Set<UUID> sourceIds) {
        return sourceIds.isEmpty()
                ? getByQuery(query)
                : getByQueryAndSourceIds(query, sourceIds);
    }

    private List<DocumentationSearchHit> getByQuery(String query) {
        return documentChunkRepository.searchCitableChunksByQuery(query).stream()
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

    private List<DocumentationSearchHit> getByQueryAndSourceIds(String query, Set<UUID> sourceIds) {
        return documentChunkRepository.searchCitableChunksByQueryAndSourceIds(query, sourceIds).stream()
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
}
