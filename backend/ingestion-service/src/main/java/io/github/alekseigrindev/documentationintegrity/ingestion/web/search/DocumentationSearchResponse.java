package io.github.alekseigrindev.documentationintegrity.ingestion.web.search;

import java.net.URI;
import java.time.Instant;
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
            UUID sourceId,
            String sourceLocator,
            URI canonicalUrl,
            String productVariant,
            String upstreamVersion,
            String mediaType,
            Instant acquiredAt,
            String documentContentHash,
            String attribution
    ) {
    }
}
