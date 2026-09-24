package io.github.alekseigrindev.documentationintegrity.ingestion.evaluation;

public record ExpectedPassage(
        String sourceLocator,
        String chunkContentHash
) {
}
