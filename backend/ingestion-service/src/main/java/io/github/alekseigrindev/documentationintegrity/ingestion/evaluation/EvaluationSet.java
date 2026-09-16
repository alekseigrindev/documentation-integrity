package io.github.alekseigrindev.documentationintegrity.ingestion.evaluation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties("corpus")
public record EvaluationSet(
        int evaluationSetVersion,
        List<EvaluationCase> cases
) {
}
