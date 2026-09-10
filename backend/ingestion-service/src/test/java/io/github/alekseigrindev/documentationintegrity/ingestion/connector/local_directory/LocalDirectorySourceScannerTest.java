package io.github.alekseigrindev.documentationintegrity.ingestion.connector.local_directory;

import io.github.alekseigrindev.documentationintegrity.ingestion.connector.AcquiredDocument;
import io.github.alekseigrindev.documentationintegrity.ingestion.connector.ConnectorType;
import io.github.alekseigrindev.documentationintegrity.ingestion.importing.DocumentationImportProperties;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.Source;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class LocalDirectorySourceScannerTest {

    @TempDir
    Path localDirectory;

    @Test
    void skipsBlankMarkdownFilesAndContinuesScanningReadableDocuments() throws Exception {
        Files.writeString(localDirectory.resolve("empty.md"), "");
        Files.writeString(localDirectory.resolve("whitespace.md"), " \n\t\n");
        Files.writeString(
                localDirectory.resolve("workflow.md"),
                "# Workflow permissions\n\nWrite permissions publish releases."
        );

        LocalDirectorySourceScanner scanner = new LocalDirectorySourceScanner(
                new DocumentationImportProperties(localDirectory, 1_024)
        );

        List<AcquiredDocument> documents;
        try (Stream<AcquiredDocument> scanned = scanner.scan(localSource())) {
            documents = scanned.toList();
        }

        assertThat(documents)
                .extracting(AcquiredDocument::sourceLocator)
                .containsExactly("workflow.md");
    }

    private Source localSource() {
        return new Source(
                UUID.randomUUID(),
                null,
                ConnectorType.LOCAL_DIRECTORY,
                "local-docs",
                "Local docs",
                null,
                localDirectory.toUri(),
                null,
                null,
                null
        );
    }
}
