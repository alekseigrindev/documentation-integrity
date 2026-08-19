package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source.revision;

import java.time.Instant;
import java.util.UUID;

public record SourceRevisionRegistrationResponse(
        UUID id,
        UUID sourceId,
        String versionIdentifier,
        String acquisitionMethod,
        Instant acquiredAt
) {
}
