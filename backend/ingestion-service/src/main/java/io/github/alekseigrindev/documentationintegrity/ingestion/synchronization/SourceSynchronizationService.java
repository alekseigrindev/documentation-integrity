package io.github.alekseigrindev.documentationintegrity.ingestion.synchronization;

import io.github.alekseigrindev.documentationintegrity.ingestion.connector.AcquiredDocument;
import io.github.alekseigrindev.documentationintegrity.ingestion.connector.SourceScanner;
import io.github.alekseigrindev.documentationintegrity.ingestion.importing.DocumentImportService;
import io.github.alekseigrindev.documentationintegrity.ingestion.run.IngestionRun;
import io.github.alekseigrindev.documentationintegrity.ingestion.run.IngestionRunService;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.Source;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.SourceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Synchronizes all supported documents belonging to one registered source.
 */
@Service
@RequiredArgsConstructor
public class SourceSynchronizationService {

    private final SourceRepository sourceRepository;
    private final IngestionRunService ingestionRunService;
    private final DocumentImportService documentImportService;
    private final List<SourceScanner> sourceScanners;

    public IngestionRun synchronize(UUID sourceId) {
        Source source = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Source was not found: " + sourceId
                ));

        UUID runId = ingestionRunService.start(source.getId());

        try (Stream<AcquiredDocument> documents = scannerFor(source).scan(source)) {
            documents.forEach(document ->
                    documentImportService.importAcquiredDocument(source, document)
            );

            ingestionRunService.succeed(runId);
            return ingestionRunService.get(runId);
        } catch (RuntimeException exception) {
            ingestionRunService.fail(runId, exception);
            throw exception;
        }
    }

    private SourceScanner scannerFor(Source source) {
        return sourceScanners.stream()
                .filter(scanner -> scanner.connectorType() == source.getConnectorType())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No source scanner is registered for connector type: "
                                + source.getConnectorType()
                ));
    }
}