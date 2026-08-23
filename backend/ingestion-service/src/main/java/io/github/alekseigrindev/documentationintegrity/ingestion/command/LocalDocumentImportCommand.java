package io.github.alekseigrindev.documentationintegrity.ingestion.command;

import java.net.URI;

/**
 * Requests one document from the configured local directory connector.
 */
public record LocalDocumentImportCommand(
        String sourceLocator,
        URI canonicalUrl,
        String productVariant
) {
}
