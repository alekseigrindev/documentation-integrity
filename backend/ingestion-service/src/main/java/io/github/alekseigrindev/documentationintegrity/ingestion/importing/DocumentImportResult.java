package io.github.alekseigrindev.documentationintegrity.ingestion.importing;

import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentationDocument;

/**
 * Reports what the import stored.
 */
public record DocumentImportResult(
        DocumentationDocument document,
        int chunkCount,
        boolean created
) {
}
