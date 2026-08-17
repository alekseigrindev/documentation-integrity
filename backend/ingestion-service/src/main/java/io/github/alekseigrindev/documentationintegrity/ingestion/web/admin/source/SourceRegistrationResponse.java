package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source;

import java.net.URI;
import java.util.UUID;

/**
 * Source registration response DTO
 */
public record SourceRegistrationResponse(
        UUID id,
        String name,
        URI authorityUrl
) {
}
