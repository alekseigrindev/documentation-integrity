package io.github.alekseigrindev.documentationintegrity.ingestion.importing;

import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentationDocument;

/**
 * Internal result of the transactional current-state write.
 */
public record DocumentStateResult(
        DocumentationDocument document,
        int chunkCount,
        ImportOutcome outcome
) {
}
