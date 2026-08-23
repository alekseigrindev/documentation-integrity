package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.run;

import io.github.alekseigrindev.documentationintegrity.ingestion.run.IngestionFailureCode;
import io.github.alekseigrindev.documentationintegrity.ingestion.run.IngestionRunStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Public operational state of one ingestion attempt.
 */
public record IngestionRunResponse(
        UUID id,
        UUID sourceId,
        IngestionRunStatus status,
        Instant startedAt,
        Instant finishedAt,
        IngestionFailureCode failureCode,
        String failureMessage
) {
}
