package io.github.alekseigrindev.documentationintegrity.ingestion.evaluation;

public record LexicalEvaluationSummary(
        int totalCases,
        int casesFoundAtTen,
        double recallAtTen,
        double mrrAtTen,
        long p50LatencyMs,
        long p95LatencyMs
) {
}
