package io.github.alekseigrindev.documentationintegrity.ingestion.embedding;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class EmbeddingConfiguration {

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(
            prefix = "documentation-integrity.embedding",
            name = "enabled",
            havingValue = "true"
    )
    OnnxNomicEmbeddingModel textEmbeddingModel(
            EmbeddingProperties properties
    ) {
        return new OnnxNomicEmbeddingModel(properties);
    }
}
