package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.publisher;

import jakarta.validation.constraints.NotBlank;

public record PublisherRegistrationRequest(
        @NotBlank String name
) {
}
