package io.github.alekseigrindev.documentationintegrity.ingestion.command;

import java.net.URI;

/**
 * Describes one documentation file to import from a configured local checkout.
 */
public record DocumentImportCommand(
        String sourceLocator,
        URI canonicalUrl,
        String productVariant
) {
}
