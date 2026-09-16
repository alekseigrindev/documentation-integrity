package io.github.alekseigrindev.documentationintegrity.ingestion.evaluation;

import java.util.List;

public record LexicalEvaluationCaseResult(
        String caseId,
        String query,
        List<String> expectedSourceLocators,
        Integer firstMatchingRank,
        long durationMs
) {
}
