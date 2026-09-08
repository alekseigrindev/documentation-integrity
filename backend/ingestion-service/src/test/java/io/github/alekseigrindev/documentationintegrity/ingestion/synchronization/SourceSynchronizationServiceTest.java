package io.github.alekseigrindev.documentationintegrity.ingestion.synchronization;

import io.github.alekseigrindev.documentationintegrity.ingestion.connector.AcquiredDocument;
import io.github.alekseigrindev.documentationintegrity.ingestion.connector.ConnectorType;
import io.github.alekseigrindev.documentationintegrity.ingestion.connector.SourceScanner;
import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentType;
import io.github.alekseigrindev.documentationintegrity.ingestion.importing.DocumentImportService;
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
        verify(ingestionRunService).fail(eq(runId), any(IllegalArgumentException.class));
        verify(ingestionRunService, never()).succeed(any());
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
        return new AcquiredDocument(
                "content/actions/first.md",
                null,
                "fpt",
                null,
                DocumentType.MARKDOWN,
                "text/markdown",
                "# First document"
        );
    }

}
