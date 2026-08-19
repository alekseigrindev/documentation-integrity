package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source;

import java.net.URI;
import java.util.UUID;

/**
 * Public representation of a registered documentation source.
 */
public record SourceResponse(
        UUID id,
        String name,
        URI authorityUrl
) {
}
