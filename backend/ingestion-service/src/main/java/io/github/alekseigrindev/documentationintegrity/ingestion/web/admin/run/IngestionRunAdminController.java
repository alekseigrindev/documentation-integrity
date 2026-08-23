package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.run;

import io.github.alekseigrindev.documentationintegrity.ingestion.run.IngestionRun;
import io.github.alekseigrindev.documentationintegrity.ingestion.run.IngestionRunService;
import io.github.alekseigrindev.documentationintegrity.ingestion.synchronization.SourceSynchronizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Exposes the observable outcome of ingestion attempts.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/ingestion-runs")
public class IngestionRunAdminController {

    private final IngestionRunService ingestionRunService;
    private final IngestionRunMapper ingestionRunMapper;
    private final SourceSynchronizationService sourceSynchronizationService;

    @PostMapping
    public ResponseEntity<IngestionRunResponse> runIngestion(
            @Valid @RequestBody IngestionRunRequest request
    ) {
        IngestionRun run = sourceSynchronizationService.synchronize(request.sourceId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ingestionRunMapper.toResponse(run));
    }

    @GetMapping("/{runId}")
    public IngestionRunResponse get(@PathVariable UUID runId) {
        return ingestionRunMapper.toResponse(ingestionRunService.get(runId));
    }

    @GetMapping
    public List<IngestionRunResponse> findBySource(
            @RequestParam UUID sourceId
    ) {
        return ingestionRunMapper.toResponses(
                ingestionRunService.findBySource(sourceId)
        );
    }
}
