package io.github.alekseigrindev.documentationintegrity.ingestion.importing;

import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentationDocument;

import java.util.UUID;

/**
 * Reports what the import stored.
 */
public record DocumentImportResult(
        UUID ingestionRunId,
        DocumentationDocument document,
        int chunkCount,
        ImportOutcome outcome
) {
}
