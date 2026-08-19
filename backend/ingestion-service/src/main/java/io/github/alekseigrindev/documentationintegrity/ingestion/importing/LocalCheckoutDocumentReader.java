package io.github.alekseigrindev.documentationintegrity.ingestion.importing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads Markdown documents from the configured trusted local documentation checkout.
 */
@Component
@RequiredArgsConstructor
public class LocalCheckoutDocumentReader {

    private final DocumentationImportProperties importProperties;

    /**
     * Verifies that the configured checkout root exists and is a directory.
     */
    public void verifyAccessible() {
        resolveCheckoutRoot();
    }

    /**
     * Reads one relative Markdown file from the configured checkout root.
     *
     * @param sourceLocator relative repository path of the Markdown file
     * @return UTF-8 content of the requested file
     */
    public String read(String sourceLocator) {
        Path relativePath = Path.of(sourceLocator);

        if (relativePath.isAbsolute()) {
            throw new IllegalArgumentException(
                    "Source locator must be a relative path: " + sourceLocator
            );
        }

        Path checkoutRoot = resolveCheckoutRoot();

        try {
            Path requestedPath = checkoutRoot.resolve(relativePath).normalize();

            if (!requestedPath.startsWith(checkoutRoot)) {
                throw new IllegalArgumentException(
                        "Source locator escapes the configured checkout root: "
                                + sourceLocator
                );
            }

            Path realRequestedPath = requestedPath.toRealPath();

            if (!realRequestedPath.startsWith(checkoutRoot)) {
                throw new IllegalArgumentException(
                        "Source locator resolves outside the configured checkout root: "
                                + sourceLocator
                );
            }

            if (!Files.isRegularFile(realRequestedPath)) {
                throw new IllegalArgumentException(
                        "Source locator does not identify a regular file: "
                                + sourceLocator
                );
            }

            if (!realRequestedPath.getFileName().toString().endsWith(".md")) {
                throw new IllegalArgumentException(
                        "Only Markdown files with the .md extension are supported: "
                                + sourceLocator
                );
            }

            return Files.readString(realRequestedPath, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Unable to read documentation file: " + sourceLocator,
                    exception
            );
        }
    }

    private Path resolveCheckoutRoot() {
        try {
            Path checkoutRoot = importProperties.checkoutRoot().toRealPath();

            if (!Files.isDirectory(checkoutRoot)) {
                throw new IllegalArgumentException(
                        "Configured documentation checkout is not a directory: "
                                + checkoutRoot
                );
            }

            return checkoutRoot;
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Unable to access the configured documentation checkout.",
                    exception
            );
        }
    }
}
