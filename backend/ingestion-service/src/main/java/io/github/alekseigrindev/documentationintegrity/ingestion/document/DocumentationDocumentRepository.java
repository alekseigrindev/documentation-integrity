package io.github.alekseigrindev.documentationintegrity.ingestion.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

public interface DocumentationDocumentRepository extends JpaRepository<DocumentationDocument, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT document
        FROM DocumentationDocument document
        WHERE document.sourceId = :sourceId
          AND document.productVariant = :productVariant
          AND document.sourceLocator = :sourceLocator
        """)
    Optional<DocumentationDocument> findCurrentForUpdate(
            @Param("sourceId") UUID sourceId,
            @Param("productVariant") String productVariant,
            @Param("sourceLocator") String sourceLocator
    );

    @Modifying
    @Query(value = """
        INSERT INTO knowledge.documents (
            id,
            source_id,
            source_locator,
            canonical_url,
            product_variant,
            upstream_version,
            media_type,
            acquired_at,
            content_hash,
            attribution,
            resolved_content
        )
        VALUES (
            :#{#document.id},
            :#{#document.sourceId},
            :#{#document.sourceLocator},
            :#{#document.canonicalUrl == null ? null : #document.canonicalUrl.toString()},
            :#{#document.productVariant},
            :#{#document.upstreamVersion},
            :#{#document.mediaType},
            :#{#document.acquiredAt},
            :#{#document.contentHash},
            :#{#document.attribution},
            :#{#document.resolvedContent}
        )
        ON CONFLICT (source_id, product_variant, source_locator) DO NOTHING
        """, nativeQuery = true)
    int insertIfAbsent(@Param("document") DocumentationDocument document);
}
