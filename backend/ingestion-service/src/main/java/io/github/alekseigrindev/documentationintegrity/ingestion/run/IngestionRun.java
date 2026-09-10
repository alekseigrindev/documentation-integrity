package io.github.alekseigrindev.documentationintegrity.ingestion.run;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Persistent operational record of one ingestion attempt.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Setter
@Table(schema = "knowledge", name = "ingestion_runs")
public class IngestionRun {

    @Id
    private UUID id;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IngestionRunStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "documents_added")
    private Long documentsAdded;

    @Column(name = "documents_updated")
    private Long documentsUpdated;

    @Column(name = "documents_removed")
    private Long documentsRemoved;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_code")
    private IngestionFailureCode failureCode;

    @Column(name = "failure_message")
    private String failureMessage;

    public void succeed(
            Instant finishedAt,
            IngestionRunChangeCounts changeCounts) {
        status = IngestionRunStatus.SUCCEEDED;
        this.finishedAt = finishedAt;
        this.documentsAdded = changeCounts.documentsAdded();
        this.documentsUpdated = changeCounts.documentsUpdated();
        this.documentsRemoved = changeCounts.documentsRemoved();
    }

    public void fail(
            IngestionFailureCode failureCode,
            String failureMessage,
            Instant finishedAt
    ) {
        status = IngestionRunStatus.FAILED;
        this.finishedAt = finishedAt;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
    }
}
