package io.github.alekseigrindev.documentationintegrity.ingestion.importing;

/**
 * Searchable chunk prepared before the persistence transaction begins.
 */
public record PreparedChunk(
        int ordinal,
        String content,
        String contentHash
) {
}
