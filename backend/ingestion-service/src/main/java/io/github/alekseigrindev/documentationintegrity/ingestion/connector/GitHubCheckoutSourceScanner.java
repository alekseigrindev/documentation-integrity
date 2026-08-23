package io.github.alekseigrindev.documentationintegrity.ingestion.connector;

import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentType;
import io.github.alekseigrindev.documentationintegrity.ingestion.importing.DocumentationImportProperties;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.Source;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Scans the approved GitHub Docs Actions content from a pinned local checkout.
 */
@Component
@RequiredArgsConstructor
public class GitHubCheckoutSourceScanner implements SourceScanner {

    private static final Path ACTIONS_DIRECTORY = Path.of("content", "actions");
    private static final String PRODUCT_VARIANT = "fpt";

    private final DocumentationImportProperties importProperties;
    private final LocalGitVersionResolver gitVersionResolver;

    @Override
    public ConnectorType connectorType() {
        return ConnectorType.GITHUB;
    }

    @Override
    public Stream<AcquiredDocument> scan(Source source) {
        if (source.getConnectorType() != ConnectorType.GITHUB) {
            throw new IllegalArgumentException(
                    "GitHub checkout scanner cannot scan source: " + source.getId()
            );
        }

        Path checkoutRoot = resolveCheckoutRoot();
        Path actionsDirectory = resolveActionsDirectory(checkoutRoot);

        try {
            return Files.walk(actionsDirectory)
                    .filter(Files::isRegularFile)
                    .filter(this::isMarkdown)
                    .sorted()
                    .map(path -> acquire(checkoutRoot, actionsDirectory, path));
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Unable to scan the GitHub Docs Actions directory.",
                    exception
            );
        }
    }

    private AcquiredDocument acquire(
            Path checkoutRoot,
            Path actionsDirectory,
            Path path
    ) {
        Path documentPath = resolveDocumentPath(actionsDirectory, path);
        enforceSize(documentPath);

        try {
            return new AcquiredDocument(
                    sourceLocator(checkoutRoot, documentPath),
                    null,
                    PRODUCT_VARIANT,
                    gitVersionResolver.resolve(checkoutRoot, documentPath).orElse(null),
                    DocumentType.MARKDOWN,
                    DocumentType.MARKDOWN.getMimeType(),
                    Files.readString(documentPath, StandardCharsets.UTF_8)
            );
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Unable to read documentation file: " + path,
                    exception
            );
        }
    }

    private Path resolveCheckoutRoot() {
        try {
            Path checkoutRoot = importProperties.checkoutRoot().toRealPath();

            if (!Files.isDirectory(checkoutRoot)) {
                throw new IllegalArgumentException(
                        "Configured documentation root is not a directory: " + checkoutRoot
                );
            }

            return checkoutRoot;
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Unable to access the configured documentation root.",
                    exception
            );
        }
    }

    private Path resolveActionsDirectory(Path checkoutRoot) {
        try {
            Path actionsDirectory = checkoutRoot.resolve(ACTIONS_DIRECTORY).toRealPath();

            if (!actionsDirectory.startsWith(checkoutRoot)
                    || !Files.isDirectory(actionsDirectory)) {
                throw new IllegalArgumentException(
                        "Configured checkout does not contain a valid Actions directory."
                );
            }

            return actionsDirectory;
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Unable to access the GitHub Docs Actions directory.",
                    exception
            );
        }
    }

    private Path resolveDocumentPath(Path actionsDirectory, Path path) {
        try {
            Path documentPath = path.toRealPath();

            if (!documentPath.startsWith(actionsDirectory)) {
                throw new IllegalArgumentException(
                        "Documentation path resolves outside the Actions directory: " + path
                );
            }

            return documentPath;
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Unable to access documentation file: " + path,
                    exception
            );
        }
    }

    private boolean isMarkdown(Path path) {
        return path.getFileName()
                .toString()
                .endsWith(DocumentType.MARKDOWN.getFileExtension());
    }

    private void enforceSize(Path path) {
        try {
            if (Files.size(path) > importProperties.maxFileBytes()) {
                throw new IllegalArgumentException(
                        "Documentation file exceeds the configured size limit: " + path
                );
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Unable to determine documentation file size: " + path,
                    exception
            );
        }
    }

    private String sourceLocator(Path checkoutRoot, Path documentPath) {
        return checkoutRoot.relativize(documentPath)
                .toString()
                .replace('\\', '/');
    }
}