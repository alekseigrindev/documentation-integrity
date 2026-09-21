package io.github.alekseigrindev.documentationintegrity.ingestion.document;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * JPA entity for one deterministic searchable paragraph.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(schema = "knowledge", name = "chunks")
public class DocumentChunk {

    @Id
    private UUID id;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(nullable = false)
    private int ordinal;

    @Column(nullable = false)
    private String content;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "embedding")
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 768)
    private float[] embedding;
}
