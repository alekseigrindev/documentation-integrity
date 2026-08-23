package io.github.alekseigrindev.documentationintegrity.ingestion.source;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SourceRepository extends JpaRepository<Source, UUID> {

    Optional<Source> findBySourceKey(String sourceKey);

    @Modifying
    @Query(value = """
        INSERT INTO knowledge.sources (
            id,
            publisher_id,
            connector_type,
            source_key,
            name,
            description,
            source_url,
            license_name,
            license_url,
            access_policy_url
        )
        VALUES (
            :#{#source.id},
            :#{#source.publisher.id},
            :#{#source.connectorType.name()},
            :#{#source.sourceKey},
            :#{#source.name},
            :#{#source.description},
            :#{#source.sourceUrl == null ? null : #source.sourceUrl.toString()},
            :#{#source.licenseName},
            :#{#source.licenseUrl == null ? null : #source.licenseUrl.toString()},
            :#{#source.accessPolicyUrl == null ? null : #source.accessPolicyUrl.toString()}
        )
        ON CONFLICT (source_key) DO NOTHING
        """, nativeQuery = true)
    int insertIfAbsent(@Param("source") Source source);

    List<Source> findAllByOrderByNameAscIdAsc();
}
