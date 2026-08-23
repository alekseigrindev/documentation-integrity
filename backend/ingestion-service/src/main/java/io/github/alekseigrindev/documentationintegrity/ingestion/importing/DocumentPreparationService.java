package io.github.alekseigrindev.documentationintegrity.ingestion.importing;

import io.github.alekseigrindev.documentationintegrity.ingestion.connector.AcquiredDocument;
import io.github.alekseigrindev.documentationintegrity.ingestion.document.ParagraphChunker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Validates, hashes, and chunks acquired content before persistence begins.
 */
@Service
@RequiredArgsConstructor
public class DocumentPreparationService {

    private final ParagraphChunker paragraphChunker;

    public PreparedDocument prepare(AcquiredDocument document) {
        List<String> paragraphs = paragraphChunker.chunk(document.content());

        if (paragraphs.isEmpty()) {
            throw new IllegalArgumentException(
                    "Documentation file contains no searchable paragraphs."
            );
        }

        List<PreparedChunk> chunks = IntStream.range(0, paragraphs.size())
                .mapToObj(ordinal -> {
                    String content = paragraphs.get(ordinal);
                    return new PreparedChunk(ordinal, content, sha256Hex(content));
                })
                .toList();

        return new PreparedDocument(
                document,
                sha256Hex(document.content()),
                chunks
        );
    }

    private String sha256Hex(String content) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available in this Java runtime.",
                    exception
            );
        }
    }
}
