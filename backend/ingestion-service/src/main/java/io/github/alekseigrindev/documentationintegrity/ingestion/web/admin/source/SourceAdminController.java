package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source;

import io.github.alekseigrindev.documentationintegrity.ingestion.source.SourceMapper;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.SourceRegistrationResult;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.SourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/sources")
public class SourceAdminController {

    private final SourceService sourceService;
    private final SourceMapper sourceMapper;

    @PostMapping
    public ResponseEntity<SourceRegistrationResponse> register(
            @Valid @RequestBody SourceRegistrationRequest request
    ) {
        SourceRegistrationResult result = sourceService.register(sourceMapper.toSourceRegistration(request));
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(sourceMapper.toResponse(result.source()));
    }

}
