package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source.revision.document;

import io.github.alekseigrindev.documentationintegrity.ingestion.importing.DocumentImportMapper;
import io.github.alekseigrindev.documentationintegrity.ingestion.importing.DocumentImportResult;
import io.github.alekseigrindev.documentationintegrity.ingestion.importing.DocumentImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * POST endpoint that imports one file for a revision.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/source-revisions/{sourceRevisionId}/documents")
public class SourceRevisionDocumentImportController {

    private final DocumentImportService documentImportService;
    private final DocumentImportMapper documentImportMapper;

    @PostMapping
    public ResponseEntity<SourceRevisionDocumentImportResponse> importDocument(
            @PathVariable UUID sourceRevisionId,
            @Valid @RequestBody SourceRevisionDocumentImportRequest request
            ) {
        DocumentImportResult result = documentImportService.importDocument(
                sourceRevisionId,
                documentImportMapper.toCommand(request));

        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;

        return ResponseEntity.status(status).body(documentImportMapper.toResponse(result));
    }

}
