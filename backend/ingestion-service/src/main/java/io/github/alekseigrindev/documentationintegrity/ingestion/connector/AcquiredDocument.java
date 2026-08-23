package io.github.alekseigrindev.documentationintegrity.ingestion.connector;

import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentType;

import java.net.URI;

/**
 * Connector-neutral document acquired from a source before preparation and persistence.
 */
public record AcquiredDocument(
        String sourceLocator,
        URI canonicalUrl,
        String productVariant,
        String upstreamVersion,
        DocumentType documentType,
        String mediaType,
        String content
) {
}
