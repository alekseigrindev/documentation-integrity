package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source;

import io.github.alekseigrindev.documentationintegrity.ingestion.connector.ConnectorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.net.URI;
import java.util.UUID;

/**
 * Source registration request DTO
 */
public record SourceRegistrationRequest(
        @NotNull UUID publisherId,
        @NotNull ConnectorType connectorType,
        @NotBlank String sourceKey,
        @NotBlank String name,
        String description,
        URI sourceUrl,
        String licenseName,
        URI licenseUrl,
        URI accessPolicyUrl
) {
}
