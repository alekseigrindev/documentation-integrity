package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source.revision.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.net.URI;

/**
 * Input: source locator, canonical URL, product variant.
 */
public record SourceRevisionDocumentImportRequest(
        /**
         * Relative path of the Markdown file inside the configured documentation checkout.
         */
        @NotBlank String sourceLocator,

        /**
         * Public canonical URL that identifies the imported documentation page.
         */
        @NotNull URI canonicalUrl,

        /**
         * Documentation variant represented by the imported file, for example {@code fpt}.
         */
        @NotBlank String productVariant
) {
}
