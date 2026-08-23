package io.github.alekseigrindev.documentationintegrity.ingestion.run;

/**
 * Operational state of one ingestion attempt.
 */
public enum IngestionRunStatus {
    RUNNING,
    SUCCEEDED,
    FAILED
}
