package io.github.alekseigrindev.documentationintegrity.ingestion.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {

    long countByDocumentId(UUID documentId);

    @Modifying
    @Query("DELETE FROM DocumentChunk chunk WHERE chunk.documentId = :documentId")
    int deleteByDocumentId(@Param("documentId") UUID documentId);

    @Query(value = """
        SELECT
            c.id AS "chunkId",
            c.ordinal AS "chunkOrdinal",
            c.content AS "content",
            c.content_hash AS "chunkContentHash",
            d.source_id AS "sourceId",
            d.source_locator AS "sourceLocator",
            d.canonical_url AS "canonicalUrl",
            d.product_variant AS "productVariant",
            d.upstream_version AS "upstreamVersion",
            d.media_type AS "mediaType",
            d.acquired_at AS "acquiredAt",
            d.content_hash AS "documentContentHash",
            d.attribution AS "attribution"
        FROM knowledge.chunks c
        JOIN knowledge.documents d ON d.id = c.document_id
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

        UUID getSourceId();

        String getSourceLocator();

        String getCanonicalUrl();

        String getProductVariant();

        String getUpstreamVersion();

        String getMediaType();

        java.time.Instant getAcquiredAt();

        String getDocumentContentHash();

        String getAttribution();
    }
}
