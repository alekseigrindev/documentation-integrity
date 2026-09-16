package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.evaluation;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LexicalEvaluationRequest(
        @NotNull UUID sourceId
) {
}
