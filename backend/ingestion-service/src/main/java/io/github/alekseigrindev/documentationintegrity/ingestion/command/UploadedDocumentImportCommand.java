package io.github.alekseigrindev.documentationintegrity.ingestion.command;

import java.net.URI;

/**
 * Carries one bounded uploaded document into the file-upload connector.
 */
public record UploadedDocumentImportCommand(
        String sourceLocator,
        URI canonicalUrl,
        String productVariant,
        String upstreamVersion,
        String originalFilename,
        String mediaType,
        byte[] content
) {
}
