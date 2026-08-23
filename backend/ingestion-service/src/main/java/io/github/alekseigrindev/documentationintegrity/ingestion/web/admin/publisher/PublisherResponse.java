package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.publisher;

import java.util.UUID;

public record PublisherResponse(
        UUID id,
        String name
) {
}
