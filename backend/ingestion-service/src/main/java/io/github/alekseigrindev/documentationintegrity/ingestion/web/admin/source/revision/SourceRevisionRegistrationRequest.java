package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source.revision;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;

public record SourceRevisionRegistrationRequest(
        @NotBlank String versionIdentifier,
        @NotBlank String acquisitionMethod,
        @NotNull Instant acquiredAt,
        @Pattern(regexp = "^[a-fA-F0-9]{64}$") String integrityHash
) {
}
