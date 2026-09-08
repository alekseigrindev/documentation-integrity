package io.github.alekseigrindev.documentationintegrity.ingestion.command;

import io.github.alekseigrindev.documentationintegrity.ingestion.connector.ConnectorType;

import java.net.URI;
import java.util.UUID;

public record SourceUpdate(
        UUID publisherId,
        ConnectorType connectorType,
        String sourceKey,
        String name,
        URI sourceUrl
) {
}
