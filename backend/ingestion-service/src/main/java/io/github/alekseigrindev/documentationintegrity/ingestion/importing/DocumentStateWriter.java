package io.github.alekseigrindev.documentationintegrity.ingestion.importing;

import io.github.alekseigrindev.documentationintegrity.ingestion.connector.AcquiredDocument;
import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentChunk;
import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentChunkRepository;
import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentationDocument;
import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentationDocumentRepository;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.Source;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Atomically writes one prepared document as the current searchable state.
 */
@Service
@RequiredArgsConstructor
public class DocumentStateWriter {

    private final DocumentationDocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;

    @Transactional
    public DocumentStateResult synchronize(
            Source source,
            PreparedDocument prepared,
            Instant acquiredAt
    ) {
        AcquiredDocument acquired = prepared.acquiredDocument();

        return documentRepository.findCurrentForUpdate(
                        source.getId(),
                        acquired.productVariant(),
                        acquired.sourceLocator()
                )
                .map(existing -> synchronizeExisting(
                        existing,
                        source,
                        prepared,
                        acquiredAt
                ))
                .orElseGet(() -> insertOrSynchronizeConcurrent(
                        source,
                        prepared,
                        acquiredAt
                ));
    }

    private DocumentStateResult synchronizeExisting(
            DocumentationDocument document,
            Source source,
            PreparedDocument prepared,
            Instant acquiredAt
    ) {
        AcquiredDocument acquired = prepared.acquiredDocument();
        String attribution = attributionFor(source);

        if (document.getContentHash().equals(prepared.contentHash())) {
            document.refreshProvenance(
                    acquired.canonicalUrl(),
                    acquired.upstreamVersion(),
                    acquired.mediaType(),
                    acquiredAt,
                    attribution
            );

            return new DocumentStateResult(
                    document,
                    Math.toIntExact(chunkRepository.countByDocumentId(document.getId())),
                    ImportOutcome.UNCHANGED
            );
        }

        document.replaceContent(
                acquired.canonicalUrl(),
                acquired.upstreamVersion(),
                acquired.mediaType(),
                acquiredAt,
                prepared.contentHash(),
                attribution,
                acquired.content()
        );
        chunkRepository.deleteByDocumentId(document.getId());
        chunkRepository.saveAll(createChunks(document.getId(), prepared.chunks()));

        return new DocumentStateResult(
                document,
                prepared.chunks().size(),
                ImportOutcome.UPDATED
        );
    }

    private DocumentStateResult insertOrSynchronizeConcurrent(
            Source source,
            PreparedDocument prepared,
            Instant acquiredAt
    ) {
        AcquiredDocument acquired = prepared.acquiredDocument();
        DocumentationDocument document = new DocumentationDocument(
                UUID.randomUUID(),
                source.getId(),
                acquired.sourceLocator(),
                acquired.canonicalUrl(),
                acquired.productVariant(),
                acquired.upstreamVersion(),
                acquired.mediaType(),
                acquiredAt,
                prepared.contentHash(),
                attributionFor(source),
                acquired.content()
        );

        if (documentRepository.insertIfAbsent(document) == 0) {
            DocumentationDocument concurrentDocument = documentRepository
                    .findCurrentForUpdate(
                            source.getId(),
                            acquired.productVariant(),
                            acquired.sourceLocator()
                    )
                    .orElseThrow(() -> new IllegalStateException(
                            "Document was not found after concurrent insertion."
                    ));

            return synchronizeExisting(
                    concurrentDocument,
                    source,
                    prepared,
                    acquiredAt
            );
        }

        chunkRepository.saveAll(createChunks(document.getId(), prepared.chunks()));

        return new DocumentStateResult(
                document,
                prepared.chunks().size(),
                ImportOutcome.CREATED
        );
    }

    private List<DocumentChunk> createChunks(
            UUID documentId,
            List<PreparedChunk> preparedChunks
    ) {
        return preparedChunks.stream()
                .map(chunk -> new DocumentChunk(
                        UUID.randomUUID(),
                        documentId,
                        chunk.ordinal(),
                        chunk.content(),
                        chunk.contentHash()
                ))
                .toList();
    }

    private String attributionFor(Source source) {
        String attribution = source.getName();

        if (source.getSourceUrl() != null) {
            attribution += ": " + source.getSourceUrl();
        }

        if (source.getLicenseName() != null) {
            attribution += "; " + source.getLicenseName();
            if (source.getLicenseUrl() != null) {
                attribution += " (" + source.getLicenseUrl() + ")";
            }
        }

        return attribution;
    }
}
