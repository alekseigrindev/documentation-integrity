package io.github.alekseigrindev.documentationintegrity.ingestion.search;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "documentation-integrity.retrieval")
public record RetrievalProperties(
        int lexicalCandidateLimit,
        int vectorCandidateLimit,
        int rerankingCandidateLimit,
        int rerankingResultLimit
) {
    public RetrievalProperties {
        if (lexicalCandidateLimit <= 0
                || vectorCandidateLimit <= 0
                || rerankingCandidateLimit <= 0
                || rerankingResultLimit <= 0) {
            throw new IllegalArgumentException(
                    "Retrieval limits must be greater than zero"
            );
        }

        if (rerankingResultLimit > rerankingCandidateLimit) {
            throw new IllegalArgumentException(
                    "Reranking result limit must not exceed candidate limit"
            );
        }
    }
}