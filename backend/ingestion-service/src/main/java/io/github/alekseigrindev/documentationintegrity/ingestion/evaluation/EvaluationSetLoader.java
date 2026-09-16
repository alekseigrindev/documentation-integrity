package io.github.alekseigrindev.documentationintegrity.ingestion.evaluation;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class EvaluationSetLoader {

    private static final String RESOURCE_PATH = "evaluation/github-actions-retrieval-cases.json";

    private final ObjectMapper objectMapper;

    public EvaluationSet load() {
        ClassPathResource resource = new ClassPathResource(RESOURCE_PATH);

        try (InputStream stream = resource.getInputStream()) {
            return objectMapper.readValue(stream, EvaluationSet.class);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to load the lexical evaluation set", e
            );
        }
    }

}
