package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.document;

import io.github.alekseigrindev.documentationintegrity.ingestion.command.UploadedDocumentImportCommand;
import io.github.alekseigrindev.documentationintegrity.ingestion.importing.DocumentImportMapper;
import io.github.alekseigrindev.documentationintegrity.ingestion.importing.DocumentImportResult;
import io.github.alekseigrindev.documentationintegrity.ingestion.importing.DocumentImportService;
import io.github.alekseigrindev.documentationintegrity.ingestion.importing.ImportOutcome;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;

/**
 * Imports documents through either supported acquisition channel.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/sources/{sourceId}/documents")
public class DocumentImportAdminController {

    private final DocumentImportService documentImportService;
    private final DocumentImportMapper documentImportMapper;

    @PostMapping("/local-directory")
    public ResponseEntity<DocumentImportResponse> importFromLocalDirectory(
            @PathVariable UUID sourceId,
            @Valid @RequestBody LocalDocumentImportRequest request
    ) {
        DocumentImportResult result = documentImportService
                .importFromLocalDirectory(
                        sourceId,
                        documentImportMapper.toCommand(request)
                );
        return response(result);
    }

    @PostMapping(value = "/file-upload", consumes = "multipart/form-data")
    public ResponseEntity<DocumentImportResponse> importUploadedFile(
            @PathVariable UUID sourceId,
            @Valid @RequestPart("metadata") UploadedDocumentImportMetadata metadata,
            @RequestPart("file") MultipartFile file
    ) {
        UploadedDocumentImportCommand command = new UploadedDocumentImportCommand(
                metadata.sourceLocator(),
                metadata.canonicalUrl(),
                metadata.productVariant(),
                metadata.upstreamVersion(),
                file.getOriginalFilename(),
                file.getContentType(),
                readBytes(file)
        );
        return response(documentImportService.importUpload(sourceId, command));
    }

    private ResponseEntity<DocumentImportResponse> response(DocumentImportResult result) {
        HttpStatus status = result.outcome() == ImportOutcome.CREATED
                ? HttpStatus.CREATED
                : HttpStatus.OK;
        return ResponseEntity.status(status).body(documentImportMapper.toResponse(result));
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to read uploaded file.", exception);
        }
    }
}
