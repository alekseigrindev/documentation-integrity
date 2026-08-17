package io.github.alekseigrindev.documentationintegrity.ingestion.source;

public record SourceRegistrationResult(
        Source source,
        boolean created
) {
}
