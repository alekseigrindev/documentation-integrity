package io.github.alekseigrindev.documentationintegrity.ingestion.source;

import io.github.alekseigrindev.documentationintegrity.ingestion.connector.ConnectorType;
import io.github.alekseigrindev.documentationintegrity.ingestion.publisher.Publisher;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(schema = "knowledge", name = "sources")
public class Source {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publisher_id", nullable = false)
    private Publisher publisher;

    @Enumerated(EnumType.STRING)
    @Column(name = "connector_type", nullable = false)
    private ConnectorType connectorType;

    @Column(name = "source_key", nullable = false)
    private String sourceKey;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "source_url")
    private URI sourceUrl;

    @Column(name = "license_name")
    private String licenseName;

    @Column(name = "license_url")
    private URI licenseUrl;

    @Column(name = "access_policy_url")
    private URI accessPolicyUrl;
}
