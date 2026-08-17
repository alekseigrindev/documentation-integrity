package io.github.alekseigrindev.documentationintegrity.ingestion.source;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

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

    @Column(nullable = false)
    private String name;

    @Column(name = "authority_url", nullable = false)
    private URI authorityUrl;

    @Column(name = "license_name", nullable = false)
    private String licenseName;

    @Column(name = "license_url", nullable = false)
    private URI licenseUrl;

    @Column(name = "access_policy_url", nullable = false)
    private URI accessPolicyUrl;
}
