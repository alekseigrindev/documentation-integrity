package io.github.alekseigrindev.documentationintegrity.ingestion.importing;

import io.github.alekseigrindev.documentationintegrity.ingestion.command.DocumentImportCommand;
import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentChunk;
import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentChunkRepository;
import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentationDocument;
import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentationDocumentRepository;
import io.github.alekseigrindev.documentationintegrity.ingestion.document.ParagraphChunker;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.Source;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.SourceRepository;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.revision.SourceRevision;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.revision.SourceRevisionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentImportService {

    private final SourceRevisionRepository sourceRevisionRepository;
    private final SourceRepository sourceRepository;
    private final DocumentationDocumentRepository documentationDocumentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final LocalCheckoutDocumentReader documentReader;
    private final ParagraphChunker paragraphChunker;

    @Transactional
    public DocumentImportResult importDocument(
            UUID sourceRevisionId,
            DocumentImportCommand command
    ) {
        SourceRevision sourceRevision = sourceRevisionRepository.findById(sourceRevisionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Source revision was not found: " + sourceRevisionId
                ));

        return documentationDocumentRepository
                .findBySourceRevisionIdAndSourceLocator(
                        sourceRevisionId,
                        command.sourceLocator()
                )
                .map(this::existingImport)
                .orElseGet(() -> createImport(sourceRevision, command));
    }

    private DocumentImportResult createImport(
            SourceRevision sourceRevision,
            DocumentImportCommand command
    ) {
        String resolvedContent = documentReader.read(command.sourceLocator());
        List<String> paragraphs = paragraphChunker.chunk(resolvedContent);

        if (paragraphs.isEmpty()) {
            throw new IllegalArgumentException(
                    "Documentation file contains no searchable paragraphs."
            );
        }

        Source source = sourceRepository.findById(sourceRevision.getSourceId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Source was not found: " + sourceRevision.getSourceId()
                ));

        DocumentationDocument document = new DocumentationDocument(
                UUID.randomUUID(),
                sourceRevision.getId(),
                command.sourceLocator(),
                command.canonicalUrl(),
                command.productVariant(),
                sha256Hex(resolvedContent),
                attributionFor(source),
                resolvedContent
        );

        int inserted = documentationDocumentRepository.insertIfAbsent(document);

        if (inserted == 0) {
            DocumentationDocument existingDocument = documentationDocumentRepository
                    .findBySourceRevisionIdAndSourceLocator(
                            sourceRevision.getId(),
                            command.sourceLocator()
                    )
                    .orElseThrow(() -> new IllegalStateException(
                            "Document was not found after concurrent import."
                    ));

            return existingImport(existingDocument);
        }

        List<DocumentChunk> chunks = createChunks(document.getId(), paragraphs);
        documentChunkRepository.saveAll(chunks);

        return new DocumentImportResult(document, chunks.size(), true);
    }

    private DocumentImportResult existingImport(
            DocumentationDocument document
    ) {
        int chunkCount = Math.toIntExact(
                documentChunkRepository.countByDocumentId(document.getId())
        );

        return new DocumentImportResult(document, chunkCount, false);
    }

    private List<DocumentChunk> createChunks(
            UUID documentId,
            List<String> paragraphs
    ) {
        return java.util.stream.IntStream.range(0, paragraphs.size())
                .mapToObj(ordinal -> {
                    String content = paragraphs.get(ordinal);

                    return new DocumentChunk(
                            UUID.randomUUID(),
                            documentId,
                            ordinal,
                            content,
                            sha256Hex(content)
                    );
                })
                .toList();
    }

    private String attributionFor(Source source) {
        return "%s: %s; %s (%s)".formatted(
                source.getName(),
                source.getAuthorityUrl(),
                source.getLicenseName(),
                source.getLicenseUrl()
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
