package io.github.alekseigrindev.documentationintegrity.ingestion.importing;

import io.github.alekseigrindev.documentationintegrity.ingestion.command.LocalDocumentImportCommand;
import io.github.alekseigrindev.documentationintegrity.ingestion.command.UploadedDocumentImportCommand;
import io.github.alekseigrindev.documentationintegrity.ingestion.connector.AcquiredDocument;
import io.github.alekseigrindev.documentationintegrity.ingestion.connector.DocumentConnector;
import io.github.alekseigrindev.documentationintegrity.ingestion.connector.FileUploadDocumentConnector;
import io.github.alekseigrindev.documentationintegrity.ingestion.connector.LocalDirectoryDocumentConnector;
import io.github.alekseigrindev.documentationintegrity.ingestion.run.IngestionRunService;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.Source;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.SourceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.UUID;

/**
 * Imports acquired documentation into the current searchable document state.
 */
@Service
@RequiredArgsConstructor
public class DocumentImportService {

    private final SourceRepository sourceRepository;
    private final LocalDirectoryDocumentConnector localDirectoryConnector;
    private final FileUploadDocumentConnector fileUploadConnector;
    private final DocumentPreparationService documentPreparationService;
    private final DocumentStateWriter documentStateWriter;
    private final IngestionRunService ingestionRunService;
    private final Clock clock;

    public DocumentImportResult importFromLocalDirectory(
            UUID sourceId,
            LocalDocumentImportCommand command
    ) {
        return importDocument(sourceId, localDirectoryConnector, command);
    }

    public DocumentImportResult importUpload(
            UUID sourceId,
            UploadedDocumentImportCommand command
    ) {
        return importDocument(sourceId, fileUploadConnector, command);
    }

    /**
     * Prepares and persists one document already acquired by a connector.
     */
    public DocumentStateResult importAcquiredDocument(
            Source source,
            AcquiredDocument acquiredDocument
    ) {
        PreparedDocument preparedDocument =
                documentPreparationService.prepare(acquiredDocument);

        return documentStateWriter.synchronize(
                source,
                preparedDocument,
                clock.instant()
        );
    }

    private <I> DocumentImportResult importDocument(
            UUID sourceId,
            DocumentConnector<I> connector,
            I input
    ) {
        Source source = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Source was not found: " + sourceId
                ));

        UUID runId = ingestionRunService.start(source.getId());

        try {
            AcquiredDocument acquiredDocument = connector.acquire(input);
            DocumentStateResult stateResult =
                    importAcquiredDocument(source, acquiredDocument);

            ingestionRunService.succeed(runId);

            return new DocumentImportResult(
                    runId,
                    stateResult.document(),
                    stateResult.chunkCount(),
                    stateResult.outcome()
            );
        } catch (RuntimeException exception) {
            ingestionRunService.fail(runId, exception);
            throw exception;
        }
    }
}