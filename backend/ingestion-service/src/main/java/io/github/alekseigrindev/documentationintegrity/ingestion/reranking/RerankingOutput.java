package io.github.alekseigrindev.documentationintegrity.ingestion.reranking;

public record RerankingOutput(
    String chunk,
    float score
) {
}
