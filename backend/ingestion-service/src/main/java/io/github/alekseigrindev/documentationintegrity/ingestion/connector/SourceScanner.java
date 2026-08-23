package io.github.alekseigrindev.documentationintegrity.ingestion.connector;

import io.github.alekseigrindev.documentationintegrity.ingestion.source.Source;

import java.util.stream.Stream;

/**
 * Acquires documentation documents from one connector-specific source.
 */
public interface SourceScanner {
    ConnectorType connectorType();

    Stream<AcquiredDocument> scan(Source source);
}
