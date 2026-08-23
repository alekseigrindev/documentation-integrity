package io.github.alekseigrindev.documentationintegrity.ingestion.importing;

/**
 * Describes how synchronization changed the current document state.
 */
public enum ImportOutcome {
    CREATED,
    UPDATED,
    UNCHANGED
}
