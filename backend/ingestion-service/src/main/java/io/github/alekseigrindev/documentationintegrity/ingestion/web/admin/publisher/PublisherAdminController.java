package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.publisher;

import io.github.alekseigrindev.documentationintegrity.ingestion.publisher.PublisherMapper;
import io.github.alekseigrindev.documentationintegrity.ingestion.publisher.PublisherRegistrationResult;
import io.github.alekseigrindev.documentationintegrity.ingestion.publisher.PublisherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/publishers")
@RequiredArgsConstructor
public class PublisherAdminController {

    private final PublisherService publisherService;
    private final PublisherMapper publisherMapper;

    @PostMapping
    public ResponseEntity<PublisherResponse> create(
            @Valid @RequestBody PublisherRegistrationRequest request
    ) {
        PublisherRegistrationResult result = publisherService.register(publisherMapper.toPublisherRegistration(request));
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(publisherMapper.toResponse(result.publisher()));
    }

    @GetMapping
    public ResponseEntity<List<PublisherResponse>> getAll() {
        return ResponseEntity.ok(publisherMapper.toResponseList(publisherService.findAll()));
    }

    @PutMapping("/{publisherId}")
    public ResponseEntity<PublisherResponse> update(
            @PathVariable UUID publisherId,
            @Valid @RequestBody PublisherRegistrationRequest request
    ) {
        return ResponseEntity.ok(publisherMapper.toResponse(
                publisherService.update(
                        publisherId, publisherMapper.toPublisherRegistration(request)
                )
        ));
    }
}
