package io.github.alekseigrindev.documentationintegrity.ingestion.importing;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;

/**
 * Reads DOCUMENTATION_CHECKOUT_ROOT from the environment.
 */
@Validated
@ConfigurationProperties(prefix = "documentation-integrity.import")
public record DocumentationImportProperties(
        /**
         * Root directory of the trusted local documentation checkout.
         */
        @NotNull Path checkoutRoot
) {
}
