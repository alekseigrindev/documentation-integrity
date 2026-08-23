package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.document;

import jakarta.validation.constraints.NotBlank;

import java.net.URI;

/**
 * Identifies one Markdown document in the configured local directory.
 */
public record LocalDocumentImportRequest(
        @NotBlank String sourceLocator,
        URI canonicalUrl,
        @NotBlank String productVariant
) {
}
