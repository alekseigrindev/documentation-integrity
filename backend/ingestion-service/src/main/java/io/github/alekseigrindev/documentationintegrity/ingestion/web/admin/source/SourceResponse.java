package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source;

import io.github.alekseigrindev.documentationintegrity.ingestion.connector.ConnectorType;

import java.net.URI;
import java.util.UUID;

/**
 * Public representation of a registered documentation source.
 */
public record SourceResponse(
        UUID id,
        UUID publisherId,
        ConnectorType connectorType,
        String sourceKey,
        String name,
        String description,
        URI sourceUrl,
        String licenseName,
        URI licenseUrl,
        URI accessPolicyUrl
) {
}
