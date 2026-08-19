package io.github.alekseigrindev.documentationintegrity.ingestion.command;

/**
 * Requests registration of one source revision by its declared version identifier.
 */
public record SourceRevisionRegistration(
        String versionIdentifier
) {
}
