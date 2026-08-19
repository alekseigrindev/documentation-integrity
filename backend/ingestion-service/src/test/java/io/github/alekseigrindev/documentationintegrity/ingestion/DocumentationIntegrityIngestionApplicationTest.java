package io.github.alekseigrindev.documentationintegrity.ingestion;

import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentChunkRepository;
import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentationDocumentRepository;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.Source;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.SourceRepository;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.revision.SourceRevision;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.revision.SourceRevisionRepository;
import io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source.SourceRegistrationRequest;
import io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source.revision.SourceRevisionRegistrationRequest;
import io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source.revision.document.SourceRevisionDocumentImportRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class DocumentationIntegrityIngestionApplicationTest {

    private static final String SOURCE_REVISION_IDENTIFIER =
            "0123456789abcdef0123456789abcdef01234567";

    private static final Path FIXTURE_ROOT = fixtureRoot();

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

    @Autowired
    private DocumentationDocumentRepository documentationDocumentRepository;

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @DynamicPropertySource
    static void configureImportRoot(DynamicPropertyRegistry registry) {
        registry.add(
                "documentation-integrity.import.checkout-root",
                () -> FIXTURE_ROOT.toString()
        );
    }

    @BeforeEach
    void clearSources() {
        documentChunkRepository.deleteAll();
        documentationDocumentRepository.deleteAll();
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
    void gettingSourcesWhenNoneAreRegisteredReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/admin/sources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void gettingSourcesReturnsPublicRepresentationsInStableOrder() throws Exception {
        URI licenseUrl = URI.create("https://example.invalid/license");
        URI accessPolicyUrl = URI.create("https://example.invalid/access-policy");

        sourceRepository.saveAll(List.of(
                new Source(
                        UUID.randomUUID(),
                        "Zulu Docs",
                        URI.create("https://zulu.example.invalid"),
                        "Test License",
                        licenseUrl,
                        accessPolicyUrl
                ),
                new Source(
                        UUID.randomUUID(),
                        "Alpha Docs",
                        URI.create("https://alpha.example.invalid"),
                        "Test License",
                        licenseUrl,
                        accessPolicyUrl
                )
        ));

        mockMvc.perform(get("/api/admin/sources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Alpha Docs"))
                .andExpect(jsonPath("$[1].name").value("Zulu Docs"));
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
                SOURCE_REVISION_IDENTIFIER
        );

        String revisionRequestJson = objectMapper.writeValueAsString(revisionRequest);

        mockMvc.perform(post("/api/admin/sources/{sourceId}/revisions", source.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(revisionRequestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.acquisitionMethod").value("LOCAL_CHECKOUT"))
                .andExpect(jsonPath("$.acquiredAt").isNotEmpty());

        mockMvc.perform(post("/api/admin/sources/{sourceId}/revisions", source.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(revisionRequestJson))
                .andExpect(status().isOk());

        assertThat(sourceRevisionRepository.count()).isEqualTo(1);
    }

    @Test
    void importingAndSearchingDocumentationReturnsCitablePassages() throws Exception {
        Source source = registerFixtureSource();
        SourceRevision sourceRevision = registerSourceRevision(source);

        SourceRevisionDocumentImportRequest importRequest =
                new SourceRevisionDocumentImportRequest(
                        "content/actions/workflow-permissions.md",
                        URI.create("https://example.invalid/documentation-integrity/"
                                + "fixtures/workflow-permissions"),
                        "fixture"
                );

        String importRequestJson = objectMapper.writeValueAsString(importRequest);

        mockMvc.perform(post(
                        "/api/admin/source-revisions/{sourceRevisionId}/documents",
                        sourceRevision.getId()
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(importRequestJson))
                .andExpect(status().isCreated());

        mockMvc.perform(post(
                        "/api/admin/source-revisions/{sourceRevisionId}/documents",
                        sourceRevision.getId()
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(importRequestJson))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/documents/search")
                        .param("q", "write permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matches.length()").value(1))
                .andExpect(jsonPath("$.matches[0].content")
                        .value("A workflow needs write permissions to publish a release."))
                .andExpect(jsonPath("$.matches[0].sourceLocator")
                        .value("content/actions/workflow-permissions.md"))
                .andExpect(jsonPath("$.matches[0].sourceRevisionVersionIdentifier")
                        .value(SOURCE_REVISION_IDENTIFIER))
                .andExpect(jsonPath("$.matches[0].attribution")
                        .value("Documentation Integrity Test Fixture: "
                                + "https://example.invalid/documentation-integrity/fixtures; "
                                + "Apache License 2.0 "
                                + "(https://www.apache.org/licenses/LICENSE-2.0)"));

        assertThat(documentationDocumentRepository.count()).isEqualTo(1);
        assertThat(documentChunkRepository.count()).isEqualTo(3);
    }

    @Test
    void contextLoads() {
    }

    private Source registerFixtureSource() throws Exception {
        URI authorityUrl = URI.create(
                "https://example.invalid/documentation-integrity/fixtures"
        );
        SourceRegistrationRequest request = new SourceRegistrationRequest(
                "Documentation Integrity Test Fixture",
                authorityUrl,
                "Apache License 2.0",
                URI.create("https://www.apache.org/licenses/LICENSE-2.0"),
                URI.create("https://example.invalid/documentation-integrity/"
                        + "fixtures/access-policy")
        );

        mockMvc.perform(post("/api/admin/sources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        return sourceRepository.findByAuthorityUrl(authorityUrl.toString())
                .orElseThrow();
    }

    private SourceRevision registerSourceRevision(Source source) throws Exception {
        SourceRevisionRegistrationRequest request = new SourceRevisionRegistrationRequest(
                SOURCE_REVISION_IDENTIFIER
        );

        mockMvc.perform(post("/api/admin/sources/{sourceId}/revisions", source.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        return sourceRevisionRepository.findBySourceIdAndVersionIdentifier(
                source.getId(),
                SOURCE_REVISION_IDENTIFIER
        ).orElseThrow();
    }

    private static Path fixtureRoot() {
        try {
            return Path.of(Objects.requireNonNull(
                    DocumentationIntegrityIngestionApplicationTest.class
                            .getClassLoader()
                            .getResource("fixtures/github-docs")
            ).toURI());
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("Fixture root URI is invalid.", exception);
        }
    }
}
