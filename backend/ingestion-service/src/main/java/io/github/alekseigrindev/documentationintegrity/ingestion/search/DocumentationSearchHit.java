package io.github.alekseigrindev.documentationintegrity.ingestion.search;

import java.net.URI;
import java.util.UUID;

/**
 * Internal full-text match with the provenance required for citation.
 */
public record DocumentationSearchHit(
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
