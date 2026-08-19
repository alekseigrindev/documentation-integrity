package io.github.alekseigrindev.documentationintegrity.ingestion.web.search;

import io.github.alekseigrindev.documentationintegrity.ingestion.search.DocumentationSearchService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            @RequestParam("q") @NotBlank String query
    ) {
        return documentationSearchMapper.toResponse(
                documentationSearchService.search(query)
        );
    }
}
