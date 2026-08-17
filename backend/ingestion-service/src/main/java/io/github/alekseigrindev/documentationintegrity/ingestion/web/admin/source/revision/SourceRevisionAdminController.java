package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source.revision;

import io.github.alekseigrindev.documentationintegrity.ingestion.source.revision.SourceRevisionMapper;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.revision.SourceRevisionRegistrationResult;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.revision.SourceRevisionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/sources/{sourceId}/revisions")
public class SourceRevisionAdminController {

    private final SourceRevisionService sourceRevisionService;
    private final SourceRevisionMapper sourceRevisionMapper;

    @PostMapping
    public ResponseEntity<SourceRevisionRegistrationResponse> register(
            @PathVariable UUID sourceId,
            @Valid @RequestBody SourceRevisionRegistrationRequest request
    ) {
        SourceRevisionRegistrationResult result = sourceRevisionService.register(sourceId, sourceRevisionMapper.toRegistration(request));
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(sourceRevisionMapper.toResponse(result.sourceRevision()));
    }
}
