package io.github.alekseigrindev.documentationintegrity.ingestion.search;

import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

/**
 * Runs full-text search and assembles cited results.
 */
@Service
@RequiredArgsConstructor
public class DocumentationSearchService {

    private final DocumentChunkRepository documentChunkRepository;

    public List<DocumentationSearchHit> search(String query) {
        return documentChunkRepository.searchCitableChunks(query).stream()
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
