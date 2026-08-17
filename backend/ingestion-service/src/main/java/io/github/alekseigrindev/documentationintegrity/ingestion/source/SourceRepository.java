package io.github.alekseigrindev.documentationintegrity.ingestion.source;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

public interface SourceRepository extends JpaRepository<Source, UUID> {

    @Query(value = """
        SELECT id, name, authority_url, license_name, license_url, access_policy_url
        FROM knowledge.sources
        WHERE authority_url = :authorityUrl
        """, nativeQuery = true)
    Optional<Source> findByAuthorityUrl(@Param("authorityUrl") String authorityUrl);

    @Modifying
    @Query(value = """
        INSERT INTO knowledge.sources (
            id,
            name,
            authority_url,
            license_name,
            license_url,
            access_policy_url
        )
        VALUES (
            :#{#source.id},
            :#{#source.name},
            :#{#source.authorityUrl.toString()},
            :#{#source.licenseName},
            :#{#source.licenseUrl.toString()},
            :#{#source.accessPolicyUrl.toString()}
        )
        ON CONFLICT (authority_url) DO NOTHING
        """, nativeQuery = true)
    int insertIfAbsent(@Param("source") Source source);
}
