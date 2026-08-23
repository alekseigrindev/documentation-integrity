package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.run;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record IngestionRunRequest(
        @NotNull UUID sourceId
) {
}
