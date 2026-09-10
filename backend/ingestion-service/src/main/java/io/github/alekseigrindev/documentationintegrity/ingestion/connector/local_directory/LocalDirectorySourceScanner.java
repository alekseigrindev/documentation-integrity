package io.github.alekseigrindev.documentationintegrity.ingestion.connector.local_directory;

import io.github.alekseigrindev.documentationintegrity.ingestion.connector.AcquiredDocument;
import io.github.alekseigrindev.documentationintegrity.ingestion.connector.ConnectorType;
import io.github.alekseigrindev.documentationintegrity.ingestion.connector.SourceScanner;
import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentType;
import io.github.alekseigrindev.documentationintegrity.ingestion.importing.DocumentationImportProperties;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.Source;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class LocalDirectorySourceScanner implements SourceScanner {

    private final static String DEFAULT_PRODUCT_VARIANT = "default";

    private final DocumentationImportProperties importProperties;

    @Override
    public ConnectorType connectorType() {
        return ConnectorType.LOCAL_DIRECTORY;
    }

    @Override
    public Stream<AcquiredDocument> scan(Source source) {
        Path localDirectory = localDirectoryFrom(source);

        try {
            return Files.walk(localDirectory)
                    .filter(Files::isRegularFile)
                    .map(path -> resolveDocumentPath(localDirectory, path))
                    .filter(this::isMarkdown)
                    .sorted()
                    .map(path -> acquire(localDirectory, path))
                    .filter(document -> !document.content().isBlank());
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Unable to scan local documentation directory: " + localDirectory,
                    e
            );
        }
    }

    private AcquiredDocument acquire(Path localDirectory, Path path) {
        enforceSize(path);

        try {
            return new AcquiredDocument(
                    sourceLocator(localDirectory, path),
                    null,
                    DEFAULT_PRODUCT_VARIANT,
                    null,
                    DocumentType.MARKDOWN,
                    DocumentType.MARKDOWN.getMimeType(),
                    Files.readString(path, StandardCharsets.UTF_8)
            );
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Unable to read documentation file: " + path,
                    exception
            );
        }
    }


    private Path localDirectoryFrom(Source source) {
        if (source.getConnectorType() != ConnectorType.LOCAL_DIRECTORY) {
            throw new IllegalArgumentException(
                    "Local-directory scanner cannot scan source: " + source.getId()
            );
        }

        URI sourceUrl = source.getSourceUrl();

        if (sourceUrl == null || !"file".equalsIgnoreCase(sourceUrl.getScheme())) {
            throw new IllegalArgumentException(
                    "A local-directory source must have a file URL: " + source.getId()
            );
        }

        try {
            Path localDirectory = Path.of(sourceUrl).toRealPath();

            if (!Files.isDirectory(localDirectory)) {
                throw new IllegalArgumentException(
                        "Local-directory source URL is not a directory: " + sourceUrl
                );
            }

            return localDirectory;
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Unable to access local documentation directory: " + sourceUrl,
                    e
            );

        }

    }

    private Path resolveDocumentPath(Path localDirectory, Path path) {
        try {
            Path documentPath = path.toRealPath();

            if (!documentPath.startsWith(localDirectory)) {
                throw new IllegalArgumentException(
                        "Documentation path resolves outside the local directory: " + path
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

    private String sourceLocator(Path localDirectory, Path path) {
        return localDirectory.relativize(path)
                .toString()
                .replace("\\", "/");
    }
}
