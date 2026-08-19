package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source.revision.document;

import java.net.URI;
import java.util.UUID;

/**
 * Output: stored document ID and imported chunk count.
 */
public record SourceRevisionDocumentImportResponse(
        UUID documentId,
        UUID sourceRevisionId,
        String sourceLocator,
        URI canonicalUrl,
        String productVariant,
        String contentHash,
        int chunkCount
) {
}
