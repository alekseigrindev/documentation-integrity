package io.github.alekseigrindev.documentationintegrity.ingestion.search;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

/**
 * Internal full-text match with the provenance required for citation.
 */
public record DocumentationSearchHit(
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
