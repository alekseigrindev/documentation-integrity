package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.document;

import jakarta.validation.constraints.NotBlank;

import java.net.URI;

/**
 * Stable identity and optional provenance supplied with one uploaded file.
 */
public record UploadedDocumentImportMetadata(
        @NotBlank String sourceLocator,
        URI canonicalUrl,
        @NotBlank String productVariant,
        String upstreamVersion
) {
}
