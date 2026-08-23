package io.github.alekseigrindev.documentationintegrity.ingestion.command;

import io.github.alekseigrindev.documentationintegrity.ingestion.connector.ConnectorType;

import java.net.URI;
import java.util.UUID;

/**
 * Describes an approved documentation source to register for ingestion.
 */
public record SourceRegistration(
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
