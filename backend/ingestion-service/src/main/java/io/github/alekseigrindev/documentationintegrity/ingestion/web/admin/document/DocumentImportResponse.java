package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.document;

import io.github.alekseigrindev.documentationintegrity.ingestion.importing.ImportOutcome;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

/**
 * Current stored state produced by either supported ingestion connector.
 */
public record DocumentImportResponse(
        UUID ingestionRunId,
        UUID documentId,
        UUID sourceId,
        String sourceLocator,
        URI canonicalUrl,
        String productVariant,
        String upstreamVersion,
        String mediaType,
        Instant acquiredAt,
        String contentHash,
        int chunkCount,
        ImportOutcome outcome
) {
}
