package io.github.alekseigrindev.documentationintegrity.ingestion.run;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface IngestionRunRepository extends JpaRepository<IngestionRun, UUID> {

    List<IngestionRun> findBySourceIdOrderByStartedAtDescIdDesc(UUID sourceId);
}
