package io.github.alekseigrindev.documentationintegrity.ingestion.web.search;

import io.github.alekseigrindev.documentationintegrity.ingestion.search.DocumentationSearchService;
import io.github.alekseigrindev.documentationintegrity.ingestion.search.RetrievalMethod;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * HTTP API for citable full-text documentation lookup.
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentationSearchController {

    private final DocumentationSearchService documentationSearchService;
    private final DocumentationSearchMapper documentationSearchMapper;

    @GetMapping("/search")
    public DocumentationSearchResponse search(
            @RequestParam("q") @NotBlank String query,
            @RequestParam(name = "sourceId", required = false) Set<UUID> sourceIds,
            @RequestParam(name = "method", defaultValue = "LEXICAL") RetrievalMethod retrievalMethod
    ) {
        return documentationSearchMapper.toResponse(
                documentationSearchService.search(
                        query,
                        sourceIds == null ? Set.of() : sourceIds,
                        retrievalMethod
                ));
    }

    @GetMapping("/retrieval-methods")
    public ResponseEntity<List<RetrievalMethodResponse>> getRetrievalMethods() {
        List<RetrievalMethodResponse> methods = documentationSearchService.getRetrievalMethods().stream()
                .map(method -> new RetrievalMethodResponse(
                        method.name(),
                        method.getDisplayName()
                )).toList();
        return ResponseEntity.ok(methods);
    }
}
