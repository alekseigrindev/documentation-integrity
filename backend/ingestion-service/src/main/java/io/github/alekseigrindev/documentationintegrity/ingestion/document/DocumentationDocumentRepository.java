package io.github.alekseigrindev.documentationintegrity.ingestion.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DocumentationDocumentRepository extends JpaRepository<DocumentationDocument, UUID> {

    Optional<DocumentationDocument> findBySourceRevisionIdAndSourceLocator(
            UUID sourceRevisionId,
            String sourceLocator
    );

    @Modifying
    @Query(value = """
        INSERT INTO knowledge.documents (
            id,
            source_revision_id,
            source_locator,
            canonical_url,
            product_variant,
            content_hash,
            attribution,
            resolved_content
        )
        VALUES (
            :#{#document.id},
            :#{#document.sourceRevisionId},
            :#{#document.sourceLocator},
            :#{#document.canonicalUrl.toString()},
            :#{#document.productVariant},
            :#{#document.contentHash},
            :#{#document.attribution},
            :#{#document.resolvedContent}
        )
        ON CONFLICT (source_revision_id, source_locator) DO NOTHING
        """, nativeQuery = true)
    int insertIfAbsent(@Param("document") DocumentationDocument document);
}
