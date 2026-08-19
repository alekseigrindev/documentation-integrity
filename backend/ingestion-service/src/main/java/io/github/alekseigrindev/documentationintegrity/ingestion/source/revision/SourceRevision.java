package io.github.alekseigrindev.documentationintegrity.ingestion.source.revision;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Table(schema = "knowledge", name = "source_revisions")
public class SourceRevision {

    @Id
    private UUID id;

    @Column(name = "source_id")
    private UUID sourceId;

    @Column(name = "version_identifier")
    private String versionIdentifier;

    @Column(name = "acquisition_method")
    private String acquisitionMethod;

    @Column(name = "acquired_at")
    private Instant acquiredAt;

}
