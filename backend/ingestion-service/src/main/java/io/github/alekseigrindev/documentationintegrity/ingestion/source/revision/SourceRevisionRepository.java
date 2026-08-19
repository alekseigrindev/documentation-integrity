package io.github.alekseigrindev.documentationintegrity.ingestion.source.revision;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SourceRevisionRepository extends JpaRepository<SourceRevision, UUID> {

    Optional<SourceRevision> findBySourceIdAndVersionIdentifier(UUID sourceId, String versionIdentifier);

    @Modifying
    @Query(value = """
        INSERT INTO knowledge.source_revisions (
            id,
            source_id,
            version_identifier,
            acquisition_method,
            acquired_at
        )
        VALUES (
            :#{#sourceRevision.id},
            :#{#sourceRevision.sourceId},
            :#{#sourceRevision.versionIdentifier},
            :#{#sourceRevision.acquisitionMethod},
            :#{#sourceRevision.acquiredAt}
        )
            ON CONFLICT (source_id, version_identifier) DO NOTHING
        """, nativeQuery = true)
    int insertIfAbsent(@Param("sourceRevision") SourceRevision sourceRevision);
}
