package io.github.alekseigrindev.documentationintegrity.ingestion.connector;

import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentType;
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

class GitHubCheckoutSourceScannerTest {

    @TempDir
    Path checkoutRoot;

    @Test
    void scansOnlyMarkdownFilesFromTheActionsScope() throws Exception {
        Path actionsDirectory = checkoutRoot.resolve("content/actions/guides");
        Files.createDirectories(actionsDirectory);

        Files.writeString(
                actionsDirectory.resolve("workflow-permissions.md"),
                "# Workflow permissions\n\nWrite permissions publish releases."
        );
        Files.writeString(actionsDirectory.resolve("ignored.txt"), "Ignore me.");
        Files.writeString(checkoutRoot.resolve("outside-actions.md"), "Ignore me too.");

        GitHubCheckoutSourceScanner scanner = new GitHubCheckoutSourceScanner(
                new DocumentationImportProperties(checkoutRoot, 1_024),
                new LocalGitVersionResolver()
        );

        List<AcquiredDocument> documents;
        try (Stream<AcquiredDocument> scanned = scanner.scan(githubSource())) {
            documents = scanned.toList();
        }

        assertThat(documents).hasSize(1);

        AcquiredDocument document = documents.getFirst();
        assertThat(document.sourceLocator())
                .isEqualTo("content/actions/guides/workflow-permissions.md");
        assertThat(document.canonicalUrl()).isNull();
        assertThat(document.productVariant()).isEqualTo("fpt");
        assertThat(document.upstreamVersion()).isNull();
        assertThat(document.documentType()).isEqualTo(DocumentType.MARKDOWN);
        assertThat(document.mediaType()).isEqualTo("text/markdown");
        assertThat(document.content())
                .contains("Write permissions publish releases.");
    }

    private Source githubSource() {
        return new Source(
                UUID.randomUUID(),
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
}