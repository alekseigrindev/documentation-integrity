package io.github.alekseigrindev.documentationintegrity.ingestion.evaluation;

import java.util.List;

public record LexicalEvaluationCaseResult(
        String caseId,
        String query,
        List<ExpectedPassage> expectedPassages,
        int relevantPassagesAtTen,
        double precisionAtTen,
        double recallAtTen,
        Integer firstMatchingRank,
        long durationMs
) {
}
