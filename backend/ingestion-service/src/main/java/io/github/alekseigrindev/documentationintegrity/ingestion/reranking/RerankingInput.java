package io.github.alekseigrindev.documentationintegrity.ingestion.reranking;

public record RerankingInput(
        String query,
        String passage
) {
}
