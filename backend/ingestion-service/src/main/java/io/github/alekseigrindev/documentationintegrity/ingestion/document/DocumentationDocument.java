package io.github.alekseigrindev.documentationintegrity.ingestion.document;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;


/**
 * JPA entity for the latest successfully imported state of one logical document.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(schema = "knowledge", name = "documents")
public class DocumentationDocument {

    @Id
    private UUID id;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Column(name = "source_locator", nullable = false)
    private String sourceLocator;

    @Column(name = "canonical_url")
    private URI canonicalUrl;

    @Column(name = "product_variant", nullable = false)
    private String productVariant;

    @Column(name = "upstream_version")
    private String upstreamVersion;

    @Column(name = "media_type", nullable = false)
    private String mediaType;

    @Column(name = "acquired_at", nullable = false)
    private Instant acquiredAt;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(nullable = false)
    private String attribution;

    @Column(name = "resolved_content", nullable = false)
    private String resolvedContent;

    public void refreshProvenance(
            URI canonicalUrl,
            String upstreamVersion,
            String mediaType,
            Instant acquiredAt,
            String attribution
    ) {
        this.canonicalUrl = canonicalUrl;
        this.upstreamVersion = upstreamVersion;
        this.mediaType = mediaType;
        this.acquiredAt = acquiredAt;
        this.attribution = attribution;
    }

    public void replaceContent(
            URI canonicalUrl,
            String upstreamVersion,
            String mediaType,
            Instant acquiredAt,
            String contentHash,
            String attribution,
            String resolvedContent
    ) {
        refreshProvenance(
                canonicalUrl,
                upstreamVersion,
                mediaType,
                acquiredAt,
                attribution
        );
        this.contentHash = contentHash;
        this.resolvedContent = resolvedContent;
    }
}
