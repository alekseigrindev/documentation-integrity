package io.github.alekseigrindev.documentationintegrity.ingestion.run;

/**
 * Stable operator-facing categories for failed ingestion attempts.
 */
public enum IngestionFailureCode {
    INVALID_DOCUMENT,
    SOURCE_UNAVAILABLE,
    PERSISTENCE_FAILURE,
    INTERNAL_ERROR
}
