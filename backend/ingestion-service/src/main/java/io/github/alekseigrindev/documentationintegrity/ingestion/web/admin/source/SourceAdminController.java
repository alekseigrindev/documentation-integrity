package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source;

import io.github.alekseigrindev.documentationintegrity.ingestion.source.SourceMapper;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.SourceRegistrationResult;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.SourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/sources")
public class SourceAdminController {

    private final SourceService sourceService;
    private final SourceMapper sourceMapper;

    @PostMapping
    public ResponseEntity<SourceResponse> register(
            @Valid @RequestBody SourceRegistrationRequest request
    ) {
        SourceRegistrationResult result = sourceService.register(sourceMapper.toSourceRegistration(request));
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(sourceMapper.toResponse(result.source()));
    }

    @GetMapping
    public ResponseEntity<List<SourceResponse>> getAll() {
        return ResponseEntity.ok(sourceMapper.toResponseList(sourceService.findAll()));
    }


}
