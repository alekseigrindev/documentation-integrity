package io.github.alekseigrindev.documentationintegrity.ingestion.connector;

/**
 * Converts one connector-specific input into the shared ingestion document.
 */
public interface DocumentConnector<I> {

    AcquiredDocument acquire(I input);
}
