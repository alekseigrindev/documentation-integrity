package io.github.alekseigrindev.documentationintegrity.ingestion.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {

    long countByDocumentId(UUID documentId);

    @Query(value = """
        SELECT
            c.id AS "chunkId",
            c.ordinal AS "chunkOrdinal",
            c.content AS "content",
            c.content_hash AS "chunkContentHash",
            d.source_locator AS "sourceLocator",
            d.canonical_url AS "canonicalUrl",
            d.product_variant AS "productVariant",
            sr.version_identifier AS "sourceRevisionVersionIdentifier",
            d.content_hash AS "documentContentHash",
            d.attribution AS "attribution"
        FROM knowledge.chunks c
        JOIN knowledge.documents d ON d.id = c.document_id
        JOIN knowledge.source_revisions sr ON sr.id = d.source_revision_id
        WHERE c.search_vector @@ websearch_to_tsquery('english', :query)
        ORDER BY
            ts_rank_cd(c.search_vector, websearch_to_tsquery('english', :query)) DESC,
            c.id
        """, nativeQuery = true)
    List<CitableChunkSearchRow> searchCitableChunks(@Param("query") String query);

    interface CitableChunkSearchRow {

        UUID getChunkId();

        int getChunkOrdinal();

        String getContent();

        String getChunkContentHash();

        String getSourceLocator();

        String getCanonicalUrl();

        String getProductVariant();

        String getSourceRevisionVersionIdentifier();

        String getDocumentContentHash();

        String getAttribution();
    }
}
