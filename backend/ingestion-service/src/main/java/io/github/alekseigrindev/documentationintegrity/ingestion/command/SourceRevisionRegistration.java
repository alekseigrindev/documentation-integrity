package io.github.alekseigrindev.documentationintegrity.ingestion.command;

import java.time.Instant;

/**
 * Describes one immutable revision of a documentation source to ingest.
 */
public record SourceRevisionRegistration(
        String versionIdentifier,
        String acquisitionMethod,
        Instant acquiredAt,
        String integrityHash
) {
}
