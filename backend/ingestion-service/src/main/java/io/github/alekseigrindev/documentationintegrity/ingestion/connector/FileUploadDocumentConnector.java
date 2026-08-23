package io.github.alekseigrindev.documentationintegrity.ingestion.connector;

import io.github.alekseigrindev.documentationintegrity.ingestion.command.UploadedDocumentImportCommand;
import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentType;
import io.github.alekseigrindev.documentationintegrity.ingestion.importing.DocumentationImportProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Acquires one bounded UTF-8 Markdown upload.
 */
@Component
@RequiredArgsConstructor
public class FileUploadDocumentConnector
        implements DocumentConnector<UploadedDocumentImportCommand> {

    private static final String SUPPORTED_MEDIA_TYPE =
            DocumentType.MARKDOWN.getMimeType();

    private final DocumentationImportProperties importProperties;

    @Override
    public AcquiredDocument acquire(UploadedDocumentImportCommand command) {
        validateSize(command.content());
        String mediaType = normalizeMediaType(command.mediaType());
        validateFilename(command.originalFilename());

        return new AcquiredDocument(
                command.sourceLocator(),
                command.canonicalUrl(),
                command.productVariant(),
                normalizeOptional(command.upstreamVersion()),
                DocumentType.MARKDOWN,
                mediaType,
                decodeUtf8(command.content())
        );
    }

    private void validateSize(byte[] content) {
        if (content.length == 0) {
            throw new IllegalArgumentException("Uploaded documentation file is empty.");
        }

        if (content.length > importProperties.maxFileBytes()) {
            throw new IllegalArgumentException(
                    "Uploaded documentation file exceeds the configured size limit."
            );
        }
    }

    private String normalizeMediaType(String value) {
        String mediaType = value == null || value.isBlank()
                ? SUPPORTED_MEDIA_TYPE
                : value.toLowerCase(Locale.ROOT).split(";", 2)[0].trim();

        if (!SUPPORTED_MEDIA_TYPE.equals(mediaType)) {
            throw new IllegalArgumentException(
                    "Only text/markdown uploads are supported."
            );
        }

        return mediaType;
    }

    private void validateFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return;
        }

        String normalized = filename.toLowerCase(Locale.ROOT);
        if (!normalized.endsWith(DocumentType.MARKDOWN.getFileExtension())) {
            throw new IllegalArgumentException(
                    "Uploaded documentation filename must end with .md."
            );
        }
    }

    private String decodeUtf8(byte[] content) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(content))
                    .toString();
        } catch (CharacterCodingException exception) {
            throw new IllegalArgumentException(
                    "Uploaded documentation must be valid UTF-8 text.",
                    exception
            );
        }
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
