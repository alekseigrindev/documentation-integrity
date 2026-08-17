package io.github.alekseigrindev.documentationintegrity.ingestion.source.revision;

public record SourceRevisionRegistrationResult(
        SourceRevision sourceRevision,
        boolean created
) {
}
