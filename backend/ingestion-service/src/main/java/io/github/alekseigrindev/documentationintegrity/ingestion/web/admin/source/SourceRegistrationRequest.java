package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.net.URI;

/**
 * Source registration request DTO
 */
public record SourceRegistrationRequest(
        @NotBlank String name,
        @NotNull URI authorityUrl,
        @NotBlank String licenseName,
        @NotNull URI licenseUrl,
        @NotNull URI accessPolicyUrl
) {
}
