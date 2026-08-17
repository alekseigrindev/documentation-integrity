package io.github.alekseigrindev.documentationintegrity.ingestion.command;

import java.net.URI;

/**
 * Describes an approved documentation authority to register for ingestion.
 */
public record SourceRegistration(
        String name,
        URI authorityUrl,
        String licenseName,
        URI licenseUrl,
        URI accessPolicyUrl
) {
}
