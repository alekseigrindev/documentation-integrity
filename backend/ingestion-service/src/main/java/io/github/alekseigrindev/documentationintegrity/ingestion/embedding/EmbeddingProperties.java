package io.github.alekseigrindev.documentationintegrity.ingestion.embedding;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "documentation-integrity.embedding")
public record EmbeddingProperties(
        boolean enabled,
        Path modelPath,
        Path tokenizerPath,
        int maxTokens,
        int batchSize
) {

    public EmbeddingProperties {
        if (maxTokens <= 0) {
            throw new IllegalArgumentException("Max tokens must be greater than 0");
        }

        if (enabled && modelPath == null) {
            throw new IllegalArgumentException("Model path must be provided when embedding is enabled");
        }

        if (enabled && tokenizerPath == null) {
            throw new IllegalArgumentException("Tokenizer path must be provided when embedding is enabled");
        }
        if (batchSize <= 0) {
            throw new IllegalArgumentException(
                    "Embedding batch size must be greater than 0"
            );
        }
    }
}
