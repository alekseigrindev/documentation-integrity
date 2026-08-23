package io.github.alekseigrindev.documentationintegrity.ingestion.importing;

import io.github.alekseigrindev.documentationintegrity.ingestion.connector.AcquiredDocument;

import java.util.List;

/**
 * Fully hashed document state ready for a short persistence transaction.
 */
public record PreparedDocument(
        AcquiredDocument acquiredDocument,
        String contentHash,
        List<PreparedChunk> chunks
) {
}
