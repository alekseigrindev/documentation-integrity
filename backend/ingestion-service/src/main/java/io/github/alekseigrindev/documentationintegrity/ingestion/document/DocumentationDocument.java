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
import java.util.UUID;


/**
 * JPA entity for one imported Markdown file and its provenance.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(schema = "knowledge", name = "documents")
public class DocumentationDocument {

    @Id
    private UUID id;

    @Column(name = "source_revision_id", nullable = false)
    private UUID sourceRevisionId;

    @Column(name = "source_locator", nullable = false)
    private String sourceLocator;

    @Column(name = "canonical_url", nullable = false)
    private URI canonicalUrl;

    @Column(name = "product_variant", nullable = false)
    private String productVariant;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(nullable = false)
    private String attribution;

    @Column(name = "resolved_content", nullable = false)
    private String resolvedContent;
}
