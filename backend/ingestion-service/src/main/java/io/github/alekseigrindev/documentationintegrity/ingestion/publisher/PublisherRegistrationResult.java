package io.github.alekseigrindev.documentationintegrity.ingestion.publisher;

public record PublisherRegistrationResult(
        Publisher publisher,
        boolean created
) {
}
