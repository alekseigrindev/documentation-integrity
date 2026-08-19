package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source.revision;

import jakarta.validation.constraints.NotBlank;

/**
 * Requests registration of a revision available in the configured local checkout.
 */
public record SourceRevisionRegistrationRequest(
        @NotBlank String versionIdentifier
) {
}
