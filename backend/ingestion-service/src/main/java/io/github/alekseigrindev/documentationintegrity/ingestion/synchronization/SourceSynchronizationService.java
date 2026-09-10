package io.github.alekseigrindev.documentationintegrity.ingestion.synchronization;

import io.github.alekseigrindev.documentationintegrity.ingestion.connector.AcquiredDocument;
import io.github.alekseigrindev.documentationintegrity.ingestion.connector.SourceScanner;
import io.github.alekseigrindev.documentationintegrity.ingestion.importing.DocumentImportService;
import io.github.alekseigrindev.documentationintegrity.ingestion.importing.DocumentStateResult;
import io.github.alekseigrindev.documentationintegrity.ingestion.run.IngestionRun;
import io.github.alekseigrindev.documentationintegrity.ingestion.run.IngestionRunChangeCounts;
import io.github.alekseigrindev.documentationintegrity.ingestion.run.IngestionRunService;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.Source;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.SourceRepository;
import io.github.alekseigrindev.documentationintegrity.ingestion.importing.ImportOutcome;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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

    @Transactional
    public IngestionRun synchronize(UUID sourceId) {
        Source source = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Source was not found: " + sourceId
                ));

        UUID runId = ingestionRunService.start(source.getId());

        try (Stream<AcquiredDocument> documents = scannerFor(source).scan(source)) {
            List<AcquiredDocument> scannedDocuments = documents.toList();

            List<DocumentStateResult> retainedDocumentResults = scannedDocuments.stream()
                    .map(document -> documentImportService.importAcquiredDocument(source, document))
                    .toList();

            Set<UUID> retainedDocumentIds = retainedDocumentResults.stream()
                    .map(documentStateResult -> documentStateResult.document().getId())
                    .collect(Collectors.toSet());

            long removedDocumentCount = documentImportService.removeDocumentsMissingFromScan(source, retainedDocumentIds);

            IngestionRunChangeCounts changeCounts = new IngestionRunChangeCounts(
                    retainedDocumentResults.stream().filter(documentStateResult -> documentStateResult.outcome() == ImportOutcome.CREATED).count(),
                    retainedDocumentResults.stream().filter(documentStateResult -> documentStateResult.outcome() == ImportOutcome.UPDATED).count(),
                        removedDocumentCount
                    );

            ingestionRunService.succeed(runId, changeCounts);
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