package io.github.alekseigrindev.documentationintegrity.ingestion;

import io.github.alekseigrindev.documentationintegrity.ingestion.source.Source;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.SourceRepository;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.revision.SourceRevision;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.revision.SourceRevisionRepository;
import io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source.SourceRegistrationRequest;
import io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source.revision.SourceRevisionRegistrationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class DocumentationIntegrityIngestionApplicationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private SourceRevisionRepository sourceRevisionRepository;

    @BeforeEach
    void clearSources() {
        sourceRevisionRepository.deleteAll();
        sourceRepository.deleteAll();
    }

    @Test
    void registeringTheSameSourceTwiceCreatesOneSource() throws Exception {
        SourceRegistrationRequest request = new SourceRegistrationRequest(
                "GitHub Docs",
                URI.create("https://docs.github.com"),
                "CC BY 4.0",
                URI.create("https://creativecommons.org/licenses/by/4.0/"),
                URI.create("https://docs.github.com/en/site-policy/github-terms/github-terms-of-service")
        );

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/admin/sources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/admin/sources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        assertThat(sourceRepository.count()).isEqualTo(1);
    }

    @Test
    void registeringTheSameSourceRevisionTwiceCreatesOneRevision() throws Exception {
        URI authorityUrl = URI.create("https://docs.github.com");

        SourceRegistrationRequest request =
                new SourceRegistrationRequest(
                        "GitHub Docs",
                        authorityUrl,
                        "CC BY 4.0",
                        URI.create("https://creativecommons.org/licenses/by/4.0/"),
                        URI.create("https://docs.github.com/en/site-policy/github-terms/github-terms-of-service")
                );

        String sourceRequestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/admin/sources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sourceRequestJson))
                .andExpect(status().isCreated());

        Source source = sourceRepository.findByAuthorityUrl(authorityUrl.toString()).orElseThrow();

        SourceRevisionRegistrationRequest revisionRequest = new SourceRevisionRegistrationRequest(
                "0123456789abcdef0123456789abcdef01234567",
                "PINNED_GIT_CHECKOUT",
                Instant.parse("2026-08-17T00:00:00Z"),
                "a".repeat(64)
        );

        String revisionRequestJson = objectMapper.writeValueAsString(revisionRequest);

        mockMvc.perform(post("/api/admin/sources/{sourceId}/revisions", source.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(revisionRequestJson))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/admin/sources/{sourceId}/revisions", source.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(revisionRequestJson))
                .andExpect(status().isOk());

        assertThat(sourceRevisionRepository.count()).isEqualTo(1);
    }

    @Test
    void contextLoads() {
    }
}
