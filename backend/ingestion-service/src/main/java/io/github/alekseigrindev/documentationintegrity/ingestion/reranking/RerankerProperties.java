package io.github.alekseigrindev.documentationintegrity.ingestion.reranking;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "documentation-integrity.reranker")
public record RerankerProperties(
    boolean enabled,
    Path modelPath,
    Path tokenizerPath,
    int maxTokens,
    int batchSize
    ) {

    public RerankerProperties {
        if (maxTokens <= 0) {
            throw new IllegalArgumentException(
                    "Reranker max tokens must be greater than 0"
            );
        }

        if (batchSize <= 0) {
            throw new IllegalArgumentException(
                    "Reranker batch size must be greater than 0"
            );
        }

        if (enabled && modelPath == null) {
            throw new IllegalArgumentException(
                    "Reranker model path must be provided when reranking is enabled"
            );
        }

        if (enabled && tokenizerPath == null) {
            throw new IllegalArgumentException(
                    "Reranker tokenizer path must be provided when reranking is enabled"
            );
        }
    }
}
