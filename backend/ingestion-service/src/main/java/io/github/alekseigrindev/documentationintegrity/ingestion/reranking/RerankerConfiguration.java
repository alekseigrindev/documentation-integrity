package io.github.alekseigrindev.documentationintegrity.ingestion.reranking;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class RerankerConfiguration {

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(
            prefix = "documentation-integrity.reranker",
            name = "enabled",
            havingValue = "true"
    )
    OnnxBgeRerankerModel textRerankingModel(
            RerankerProperties properties
    ) {
        return new OnnxBgeRerankerModel(properties);
    }
}
