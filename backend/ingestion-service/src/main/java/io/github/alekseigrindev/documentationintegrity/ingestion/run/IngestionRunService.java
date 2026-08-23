package io.github.alekseigrindev.documentationintegrity.ingestion.run;

import io.github.alekseigrindev.documentationintegrity.ingestion.source.SourceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.UncheckedIOException;
import java.time.Clock;
import java.util.List;
import java.util.UUID;

/**
 * Starts and completes ingestion audit records in independent transactions.
 */
@Service
@RequiredArgsConstructor
public class IngestionRunService {

    private static final int MAX_FAILURE_MESSAGE_LENGTH = 1000;

    private final IngestionRunRepository ingestionRunRepository;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UUID start(UUID sourceId) {
        IngestionRun run = new IngestionRun(
                UUID.randomUUID(),
                sourceId,
                IngestionRunStatus.RUNNING,
                clock.instant(),
                null,
                null,
                null
        );
        return ingestionRunRepository.save(run).getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void succeed(UUID runId) {
        find(runId).succeed(clock.instant());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(UUID runId, RuntimeException exception) {
        find(runId).fail(
                failureCode(exception),
                boundedMessage(exception),
                clock.instant()
        );
    }

    @Transactional(readOnly = true)
    public IngestionRun get(UUID runId) {
        return find(runId);
    }

    @Transactional(readOnly = true)
    public List<IngestionRun> findBySource(UUID sourceId) {
        return ingestionRunRepository.findBySourceIdOrderByStartedAtDescIdDesc(sourceId);
    }

    private IngestionRun find(UUID runId) {
        return ingestionRunRepository.findById(runId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Ingestion run was not found: " + runId
                ));
    }

    private String boundedMessage(RuntimeException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            message = exception.getClass().getSimpleName();
        }
        return message.substring(0, Math.min(message.length(), MAX_FAILURE_MESSAGE_LENGTH));
    }

    private IngestionFailureCode failureCode(RuntimeException exception) {
        if (exception instanceof IllegalArgumentException) {
            return IngestionFailureCode.INVALID_DOCUMENT;
        }
        if (exception instanceof UncheckedIOException) {
            return IngestionFailureCode.SOURCE_UNAVAILABLE;
        }
        if (exception instanceof DataAccessException) {
            return IngestionFailureCode.PERSISTENCE_FAILURE;
        }
        return IngestionFailureCode.INTERNAL_ERROR;
    }




}
