package io.github.alekseigrindev.documentationintegrity.ingestion.web.search;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Returned passage and reproducible citation metadata.
 */
public record DocumentationSearchResponse(
        List<Match> matches
) {
    public record Match(
            UUID chunkId,
            int chunkOrdinal,
            String content,
            String chunkContentHash,
            String sourceLocator,
            URI canonicalUrl,
            String productVariant,
            String sourceRevisionVersionIdentifier,
            String documentContentHash,
            String attribution
    ) {
    }
}
