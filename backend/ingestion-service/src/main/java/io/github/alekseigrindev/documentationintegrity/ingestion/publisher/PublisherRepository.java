package io.github.alekseigrindev.documentationintegrity.ingestion.publisher;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PublisherRepository extends JpaRepository<Publisher, UUID> {

    boolean existsByName(String name);

    List<Publisher> findAllByOrderByNameAscIdAsc();

    Optional<Publisher> findByName(String name);

    @Modifying
    @Query(value = """
        INSERT INTO knowledge.publishers (
            id,
            name
        )
        VALUES (
            :#{#publisher.id},
            :#{#publisher.name}
        )
        ON CONFLICT (name) DO NOTHING
    """, nativeQuery = true)
    int insertIfAbsent(@Param("publisher") Publisher publisher);
}
