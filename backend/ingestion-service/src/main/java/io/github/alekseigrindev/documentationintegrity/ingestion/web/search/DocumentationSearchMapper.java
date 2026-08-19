package io.github.alekseigrindev.documentationintegrity.ingestion.web.search;

import io.github.alekseigrindev.documentationintegrity.ingestion.search.DocumentationSearchHit;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DocumentationSearchMapper {
    DocumentationSearchResponse.Match toMatch(
            DocumentationSearchHit hit
    );

    List<DocumentationSearchResponse.Match> toMatches(
            List<DocumentationSearchHit> hits
    );

    default DocumentationSearchResponse toResponse(
            List<DocumentationSearchHit> hits
    ) {
        return new DocumentationSearchResponse(toMatches(hits));
    }

}
