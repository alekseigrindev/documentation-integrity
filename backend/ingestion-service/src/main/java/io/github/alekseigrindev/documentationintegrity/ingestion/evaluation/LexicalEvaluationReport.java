package io.github.alekseigrindev.documentationintegrity.ingestion.evaluation;

import java.util.List;
import java.util.UUID;

public record LexicalEvaluationReport(
        UUID sourceId,
        int evaluationSetVersion,
        List<LexicalEvaluationCaseResult> caseResults,
        LexicalEvaluationSummary summary
) {
}
