package io.github.alekseigrindev.documentationintegrity.ingestion.evaluation;

import java.util.List;

public record EvaluationCase(
        String id,
        String query,
        List<String> expectedSourceLocators
) {
}
