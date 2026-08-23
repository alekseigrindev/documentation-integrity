package io.github.alekseigrindev.documentationintegrity.ingestion.connector;

import io.github.alekseigrindev.documentationintegrity.ingestion.command.LocalDocumentImportCommand;
import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentType;
import io.github.alekseigrindev.documentationintegrity.ingestion.importing.DocumentationImportProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Acquires Markdown documents from the configured trusted local directory.
 */
@Component
@RequiredArgsConstructor
public class LocalDirectoryDocumentConnector
        implements DocumentConnector<LocalDocumentImportCommand> {

    private final DocumentationImportProperties importProperties;
    private final LocalGitVersionResolver gitVersionResolver;

    @Override
    public AcquiredDocument acquire(LocalDocumentImportCommand command) {
        Path root = resolveRoot();
        Path requestedPath = resolveDocument(root, command.sourceLocator());
        enforceSize(requestedPath);

        try {
            return new AcquiredDocument(
                    command.sourceLocator(),
                    command.canonicalUrl(),
                    command.productVariant(),
                    gitVersionResolver.resolve(root, requestedPath).orElse(null),
                    DocumentType.MARKDOWN,
                    DocumentType.MARKDOWN.getMimeType(),
                    Files.readString(requestedPath, StandardCharsets.UTF_8)
            );
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Unable to read documentation file: " + command.sourceLocator(),
                    exception
            );
        }
    }

    private Path resolveDocument(Path root, String sourceLocator) {
        Path relativePath = Path.of(sourceLocator);

        if (relativePath.isAbsolute()) {
            throw new IllegalArgumentException(
                    "Source locator must be a relative path: " + sourceLocator
            );
        }

        try {
            Path requestedPath = root.resolve(relativePath).normalize();

            if (!requestedPath.startsWith(root)) {
                throw new IllegalArgumentException(
                        "Source locator escapes the configured directory: "
                                + sourceLocator
                );
            }

            Path realPath = requestedPath.toRealPath();

            if (!realPath.startsWith(root)) {
                throw new IllegalArgumentException(
                        "Source locator resolves outside the configured directory: "
                                + sourceLocator
                );
            }

            if (!Files.isRegularFile(realPath)) {
                throw new IllegalArgumentException(
                        "Source locator does not identify a regular file: "
                                + sourceLocator
                );
            }

            if (!realPath.getFileName()
                    .toString()
                    .endsWith(DocumentType.MARKDOWN.getFileExtension())) {
                throw new IllegalArgumentException(
                        "The local-directory connector supports only .md files: "
                                + sourceLocator
                );
            }

            return realPath;
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Unable to access documentation file: " + sourceLocator,
                    exception
            );
        }
    }

    private Path resolveRoot() {
        try {
            Path root = importProperties.checkoutRoot().toRealPath();

            if (!Files.isDirectory(root)) {
                throw new IllegalArgumentException(
                        "Configured documentation root is not a directory: " + root
                );
            }

            return root;
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Unable to access the configured documentation root.",
                    exception
            );
        }
    }

    private void enforceSize(Path path) {
        try {
            long size = Files.size(path);

            if (size > importProperties.maxFileBytes()) {
                throw new IllegalArgumentException(
                        "Documentation file exceeds the configured size limit."
                );
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Unable to determine documentation file size.",
                    exception
            );
        }
    }

}
