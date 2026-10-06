package io.github.alekseigrindev.documentationintegrity.ingestion.evaluation;

import io.github.alekseigrindev.documentationintegrity.ingestion.search.RetrievalMethod;
import io.github.alekseigrindev.documentationintegrity.ingestion.search.RetrievalProperties;

import java.util.List;
import java.util.UUID;

public record LexicalEvaluationReport(
        UUID sourceId,
        int evaluationSetVersion,
        RetrievalMethod retrievalMethod,
        RetrievalProperties retrievalConfiguration,
        List<LexicalEvaluationCaseResult> caseResults,
        LexicalEvaluationSummary summary
) {
}
