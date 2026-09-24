package io.github.alekseigrindev.documentationintegrity.ingestion.evaluation;

public record LexicalEvaluationSummary(
        int totalCases,
        int casesFoundAtTen,
        double hitRateAtTen,
        double meanPrecisionAtTen,
        double meanRecallAtTen,
        double mrrAtTen,
        long p50LatencyMs,
        long p95LatencyMs
) {
}
