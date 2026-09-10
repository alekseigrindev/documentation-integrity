package io.github.alekseigrindev.documentationintegrity.ingestion.synchronization;

import io.github.alekseigrindev.documentationintegrity.ingestion.connector.AcquiredDocument;
import io.github.alekseigrindev.documentationintegrity.ingestion.connector.ConnectorType;
import io.github.alekseigrindev.documentationintegrity.ingestion.connector.SourceScanner;
import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentationDocument;
import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentType;
import io.github.alekseigrindev.documentationintegrity.ingestion.importing.DocumentImportService;
import io.github.alekseigrindev.documentationintegrity.ingestion.importing.DocumentStateResult;
import io.github.alekseigrindev.documentationintegrity.ingestion.importing.ImportOutcome;
import io.github.alekseigrindev.documentationintegrity.ingestion.run.IngestionRun;
import io.github.alekseigrindev.documentationintegrity.ingestion.run.IngestionRunChangeCounts;
import io.github.alekseigrindev.documentationintegrity.ingestion.run.IngestionRunService;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.Source;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.SourceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SourceSynchronizationServiceTest {

    @Mock
    private SourceRepository sourceRepository;

    @Mock
    private IngestionRunService ingestionRunService;

    @Mock
    private DocumentImportService documentImportService;

    @Mock
    private SourceScanner sourceScanner;

    @Mock
    private IngestionRun ingestionRun;

    @Test
    void successfulScanRecordsAddedUpdatedAndRemovedDocumentCounts() {
        UUID sourceId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        Source source = source(sourceId);
        AcquiredDocument createdDocument = document("created.md");
        AcquiredDocument updatedDocument = document("updated.md");
        AcquiredDocument unchangedDocument = document("unchanged.md");

        SourceSynchronizationService synchronizationService =
                new SourceSynchronizationService(
                        sourceRepository,
                        ingestionRunService,
                        documentImportService,
                        List.of(sourceScanner)
                );

        when(sourceRepository.findById(sourceId)).thenReturn(Optional.of(source));
        when(ingestionRunService.start(sourceId)).thenReturn(runId);
        when(sourceScanner.connectorType()).thenReturn(ConnectorType.GITHUB);
        when(sourceScanner.scan(source)).thenReturn(Stream.of(
                createdDocument,
                updatedDocument,
                unchangedDocument
        ));
        when(documentImportService.importAcquiredDocument(source, createdDocument))
                .thenReturn(documentStateResult(ImportOutcome.CREATED));
        when(documentImportService.importAcquiredDocument(source, updatedDocument))
                .thenReturn(documentStateResult(ImportOutcome.UPDATED));
        when(documentImportService.importAcquiredDocument(source, unchangedDocument))
                .thenReturn(documentStateResult(ImportOutcome.UNCHANGED));
        when(documentImportService.removeDocumentsMissingFromScan(eq(source), anySet()))
                .thenReturn(2L);
        when(ingestionRunService.get(runId)).thenReturn(ingestionRun);

        synchronizationService.synchronize(sourceId);

        verify(ingestionRunService).succeed(
                runId,
                new IngestionRunChangeCounts(1, 1, 2)
        );
    }

    @Test
    void failedScanDoesNotStartReplacingTheCurrentDocumentation() {
        UUID sourceId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        Source source = source(sourceId);

        SourceSynchronizationService synchronizationService =
                new SourceSynchronizationService(
                        sourceRepository,
                        ingestionRunService,
                        documentImportService,
                        List.of(sourceScanner)
        );

        when(sourceRepository.findById(sourceId)).thenReturn(Optional.of(source));
        when(ingestionRunService.start(sourceId)).thenReturn(runId);
        when(sourceScanner.connectorType()).thenReturn(ConnectorType.GITHUB);
        when(sourceScanner.scan(source)).thenReturn(Stream.concat(
                Stream.of(document()),
                Stream.<AcquiredDocument>generate(() -> {
                    throw new IllegalArgumentException("Fixture scan failed");
                })
        ));

        assertThatThrownBy(() -> synchronizationService.synchronize(sourceId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Fixture scan failed");

        verify(documentImportService, never()).importAcquiredDocument(any(), any());
        verify(documentImportService, never()).removeDocumentsMissingFromScan(any(), anySet());
        verify(ingestionRunService).fail(eq(runId), any(IllegalArgumentException.class));
        verify(ingestionRunService, never()).succeed(any(), any());
    }

    private Source source(UUID sourceId) {
        return new Source(
                sourceId,
                null,
                ConnectorType.GITHUB,
                "github-docs",
                "GitHub Docs",
                null,
                null,
                null,
                null,
                null
        );
    }

    private AcquiredDocument document() {
        return document("first.md");
    }

    private AcquiredDocument document(String fileName) {
        return new AcquiredDocument(
                "content/actions/" + fileName,
                null,
                "fpt",
                null,
                DocumentType.MARKDOWN,
                "text/markdown",
                "# First document"
        );
    }

    private DocumentStateResult documentStateResult(ImportOutcome outcome) {
        DocumentationDocument document = mock(DocumentationDocument.class);
        when(document.getId()).thenReturn(UUID.randomUUID());

        return new DocumentStateResult(document, 1, outcome);
    }

}
