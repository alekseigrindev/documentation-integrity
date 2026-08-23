package io.github.alekseigrindev.documentationintegrity.ingestion;

import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentChunkRepository;
import io.github.alekseigrindev.documentationintegrity.ingestion.importing.ImportOutcome;
import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentationDocument;
import io.github.alekseigrindev.documentationintegrity.ingestion.document.DocumentationDocumentRepository;
import io.github.alekseigrindev.documentationintegrity.ingestion.run.IngestionRun;
import io.github.alekseigrindev.documentationintegrity.ingestion.run.IngestionFailureCode;
import io.github.alekseigrindev.documentationintegrity.ingestion.run.IngestionRunRepository;
import io.github.alekseigrindev.documentationintegrity.ingestion.run.IngestionRunStatus;
import io.github.alekseigrindev.documentationintegrity.ingestion.connector.ConnectorType;
import io.github.alekseigrindev.documentationintegrity.ingestion.publisher.Publisher;
import io.github.alekseigrindev.documentationintegrity.ingestion.publisher.PublisherRepository;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.Source;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.SourceRepository;
import io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.document.LocalDocumentImportRequest;
import io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.document.UploadedDocumentImportMetadata;
import io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source.SourceRegistrationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class DocumentationIntegrityIngestionApplicationTest {

    private static final Path FIXTURE_ROOT = fixtureRoot();

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private DocumentationDocumentRepository documentRepository;

    @Autowired
    private DocumentChunkRepository chunkRepository;

    @Autowired
    private IngestionRunRepository ingestionRunRepository;

    @DynamicPropertySource
    static void configureImport(DynamicPropertyRegistry registry) {
        registry.add(
                "documentation-integrity.import.checkout-root",
                () -> FIXTURE_ROOT.toString()
        );
        registry.add(
                "documentation-integrity.import.max-file-bytes",
                () -> "1048576"
        );
    }

    @BeforeEach
    void clearDatabase() {
        chunkRepository.deleteAll();
        documentRepository.deleteAll();
        ingestionRunRepository.deleteAll();
        sourceRepository.deleteAll();
        publisherRepository.deleteAll();
    }

    @Test
    void registeringTheSameSourceTwiceCreatesOneSource() throws Exception {
        Publisher publisher = createPublisher("GitHub Inc.");
        SourceRegistrationRequest request = sourceRequest(
                publisher.getId(),
                "github-docs",
                "GitHub Docs",
                URI.create("https://docs.github.com")
        );
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/admin/sources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publisherId").value(publisher.getId().toString()))
                .andExpect(jsonPath("$.connectorType").value("github"))
                .andExpect(jsonPath("$.sourceKey").value("github-docs"));

        mockMvc.perform(post("/api/admin/sources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        assertThat(sourceRepository.count()).isEqualTo(1);
    }

    @Test
    void registeringSourceForUnknownPublisherReturnsNotFound() throws Exception {
        SourceRegistrationRequest request = sourceRequest(
                UUID.randomUUID(),
                "github-docs",
                "GitHub Docs",
                URI.create("https://docs.github.com")
        );

        mockMvc.perform(post("/api/admin/sources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        assertThat(sourceRepository.count()).isZero();
    }

    @Test
    void gettingSourcesWhenNoneAreRegisteredReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/admin/sources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void gettingSourcesReturnsSourcesInStableOrder() throws Exception {
        Publisher publisher = createPublisher("Test Publisher");
        URI licenseUrl = URI.create("https://example.invalid/license");
        URI accessPolicyUrl = URI.create("https://example.invalid/access-policy");

        sourceRepository.saveAll(List.of(
                new Source(
                        UUID.randomUUID(),
                        publisher,
                        ConnectorType.GITHUB,
                        "zulu-docs",
                        "Zulu Docs",
                        null,
                        URI.create("https://zulu.example.invalid"),
                        "Test License",
                        licenseUrl,
                        accessPolicyUrl
                ),
                new Source(
                        UUID.randomUUID(),
                        publisher,
                        ConnectorType.GITHUB,
                        "alpha-docs",
                        "Alpha Docs",
                        null,
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
    void registeringSourceAllowsMissingOptionalMetadata() throws Exception {
        Publisher publisher = createPublisher("Local Documentation Team");
        SourceRegistrationRequest request = new SourceRegistrationRequest(
                publisher.getId(),
                ConnectorType.GITHUB,
                "local-runbooks",
                "Local runbooks",
                "Operator-maintained runbooks.",
                null,
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/admin/sources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sourceUrl").doesNotExist())
                .andExpect(jsonPath("$.licenseName").doesNotExist());

        assertThat(sourceRepository.count()).isEqualTo(1);
    }

    @Test
    void localDirectoryImportIsIdempotentAndReturnsCitablePassages()
            throws Exception {
        Source source = registerSource(
                "Documentation Integrity Test Fixture",
                URI.create("https://example.invalid/documentation-integrity/fixtures")
        );
        LocalDocumentImportRequest request = new LocalDocumentImportRequest(
                "content/actions/workflow-permissions.md",
                URI.create("https://example.invalid/documentation-integrity/"
                        + "fixtures/workflow-permissions"),
                "fixture"
        );

        MvcResult created = mockMvc.perform(post(
                        "/api/admin/sources/{sourceId}/documents/local-directory",
                        source.getId()
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.outcome").value("CREATED"))
                .andReturn();

        UUID firstDocumentId = responseUuid(created, "documentId");
        UUID firstRunId = responseUuid(created, "ingestionRunId");
        JsonNode createdResponse = responseJson(created);
        assertThat(createdResponse.get("upstreamVersion").isNull()).isTrue();

        mockMvc.perform(post(
                        "/api/admin/sources/{sourceId}/documents/local-directory",
                        source.getId()
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.outcome").value("UNCHANGED"))
                .andExpect(jsonPath("$.documentId").value(firstDocumentId.toString()));

        mockMvc.perform(get("/api/documents/search")
                        .param("q", "write permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matches.length()").value(1))
                .andExpect(jsonPath("$.matches[0].content")
                        .value("A workflow needs write permissions to publish a release."))
                .andExpect(jsonPath("$.matches[0].sourceLocator")
                        .value("content/actions/workflow-permissions.md"))
                .andExpect(jsonPath("$.matches[0].sourceId")
                        .value(source.getId().toString()))
                .andExpect(jsonPath("$.matches[0].mediaType")
                        .value("text/markdown"))
                .andExpect(jsonPath("$.matches[0].upstreamVersion").doesNotExist())
                .andExpect(jsonPath("$.matches[0].acquiredAt").isNotEmpty())
                .andExpect(jsonPath("$.matches[0].attribution")
                        .value("Documentation Integrity Test Fixture: "
                                + "https://example.invalid/documentation-integrity/fixtures; "
                                + "Apache License 2.0 "
                                + "(https://www.apache.org/licenses/LICENSE-2.0)"));

        mockMvc.perform(get("/api/admin/ingestion-runs/{runId}", firstRunId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCEEDED"));

        assertThat(documentRepository.count()).isEqualTo(1);
        assertThat(chunkRepository.count()).isEqualTo(3);
        assertThat(ingestionRunRepository.count()).isEqualTo(2);
    }

    @Test
    void changedUploadReplacesChunksWithoutChangingLogicalDocumentIdentity()
            throws Exception {
        Source source = registerSource(
                "Uploaded Test Documentation",
                URI.create("https://uploads.example.invalid/documentation")
        );
        UploadedDocumentImportMetadata metadata = new UploadedDocumentImportMetadata(
                "guides/deployment.md",
                null,
                "fixture",
                null
        );

        MvcResult created = upload(
                source.getId(),
                metadata,
                "# Deployment\n\nUse the legacy token for deployment."
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.outcome").value("CREATED"))
                .andExpect(jsonPath("$.canonicalUrl").doesNotExist())
                .andReturn();

        UUID documentId = responseUuid(created, "documentId");

        MvcResult updated = upload(
                source.getId(),
                metadata,
                "# Deployment\n\nUse OIDC federation for deployment."
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.outcome").value("UPDATED"))
                .andExpect(jsonPath("$.documentId").value(documentId.toString()))
                .andReturn();

        UUID updatedRunId = responseUuid(updated, "ingestionRunId");

        mockMvc.perform(get("/api/documents/search").param("q", "legacy token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matches.length()").value(0));

        mockMvc.perform(get("/api/documents/search").param("q", "OIDC federation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matches.length()").value(1))
                .andExpect(jsonPath("$.matches[0].sourceLocator")
                        .value("guides/deployment.md"));

        IngestionRun updatedRun = ingestionRunRepository.findById(updatedRunId)
                .orElseThrow();
        assertThat(updatedRun.getStatus()).isEqualTo(IngestionRunStatus.SUCCEEDED);
        assertThat(documentRepository.count()).isEqualTo(1);
        assertThat(chunkRepository.countByDocumentId(documentId)).isEqualTo(2);
    }

    @Test
    void bothInputPathsCanSynchronizeTheSameSource()
            throws Exception {
        Source source = registerSource(
                "Fixture Documentation",
                URI.create("https://fixture.example.invalid/documentation")
        );
        String locator = "content/actions/workflow-permissions.md";
        String content = Files.readString(FIXTURE_ROOT.resolve(locator));

        MvcResult localResult = mockMvc.perform(post(
                        "/api/admin/sources/{sourceId}/documents/local-directory",
                        source.getId()
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LocalDocumentImportRequest(
                                        locator,
                                        null,
                                        "fixture"
                                )
                        )))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult uploadResult = upload(
                source.getId(),
                new UploadedDocumentImportMetadata(
                        locator,
                        null,
                        "fixture",
                        null
                ),
                content
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.outcome").value("UNCHANGED"))
                .andReturn();

        JsonNode localResponse = responseJson(localResult);
        JsonNode uploadResponse = responseJson(uploadResult);
        assertThat(uploadResponse.get("contentHash").asText())
                .isEqualTo(localResponse.get("contentHash").asText());
        assertThat(uploadResponse.get("chunkCount").asInt())
                .isEqualTo(localResponse.get("chunkCount").asInt());
        assertThat(uploadResponse.get("mediaType").asText())
                .isEqualTo(localResponse.get("mediaType").asText());
    }

    @Test
    void failedUploadIsObservableAndLeavesCurrentDocumentSearchable()
            throws Exception {
        Source source = registerSource(
                "Uploaded Test Documentation",
                URI.create("https://uploads.example.invalid/documentation")
        );
        UploadedDocumentImportMetadata metadata = new UploadedDocumentImportMetadata(
                "guides/deployment.md",
                null,
                "fixture",
                null
        );

        upload(
                source.getId(),
                metadata,
                "# Deployment\n\nKeep this searchable guidance."
        ).andExpect(status().isCreated());

        MockMultipartFile metadataPart = metadataPart(metadata);
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "deployment.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "not a supported document".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart(
                        "/api/admin/sources/{sourceId}/documents/file-upload",
                        source.getId()
                )
                        .file(metadataPart)
                        .file(invalidFile))
                .andExpect(status().isBadRequest());

        List<IngestionRun> runs = ingestionRunRepository.findAll();
        assertThat(runs).hasSize(2);
        assertThat(runs).anySatisfy(run -> {
            assertThat(run.getStatus()).isEqualTo(IngestionRunStatus.FAILED);
            assertThat(run.getFailureCode()).isEqualTo(IngestionFailureCode.INVALID_DOCUMENT);
            assertThat(run.getFailureMessage()).contains("text/markdown");
        });

        mockMvc.perform(get("/api/documents/search").param("q", "searchable guidance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matches.length()").value(1));

        mockMvc.perform(get("/api/admin/ingestion-runs")
                        .param("sourceId", source.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("FAILED"))
                .andExpect(jsonPath("$[0].failureCode")
                        .value("INVALID_DOCUMENT"));
        assertThat(documentRepository.count()).isEqualTo(1);
    }

    @Test
    void contextLoads() {
    }

    private org.springframework.test.web.servlet.ResultActions upload(
            UUID sourceId,
            UploadedDocumentImportMetadata metadata,
            String content
    ) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "deployment.md",
                "text/markdown",
                content.getBytes(StandardCharsets.UTF_8)
        );

        return mockMvc.perform(multipart(
                        "/api/admin/sources/{sourceId}/documents/file-upload",
                        sourceId
                )
                .file(metadataPart(metadata))
                .file(file));
    }

    private MockMultipartFile metadataPart(UploadedDocumentImportMetadata metadata)
            throws Exception {
        return new MockMultipartFile(
                "metadata",
                "metadata.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(metadata)
        );
    }

    private UUID responseUuid(MvcResult result, String field) throws Exception {
        return UUID.fromString(responseText(result, field));
    }

    private String responseText(MvcResult result, String field) throws Exception {
        return responseJson(result).get(field).asText();
    }

    private JsonNode responseJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private Source registerSource(
            String name,
            URI sourceUrl
    ) throws Exception {
        Publisher publisher = createPublisher(name + " Publisher");
        String sourceKey = sourceKeyFor(name);
        SourceRegistrationRequest request = sourceRequest(
                publisher.getId(),
                sourceKey,
                name,
                sourceUrl
        );

        mockMvc.perform(post("/api/admin/sources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        return sourceRepository.findBySourceKey(sourceKey)
                .orElseThrow();
    }

    private SourceRegistrationRequest sourceRequest(
            UUID publisherId,
            String sourceKey,
            String name,
            URI sourceUrl
    ) {
        return new SourceRegistrationRequest(
                publisherId,
                ConnectorType.GITHUB,
                sourceKey,
                name,
                "Approved source for " + name + ".",
                sourceUrl,
                "Apache License 2.0",
                URI.create("https://www.apache.org/licenses/LICENSE-2.0"),
                URI.create("https://example.invalid/access-policy")
        );
    }

    private String sourceKeyFor(String name) {
        return name.toLowerCase(java.util.Locale.ROOT).replace(' ', '-');
    }

    private Publisher createPublisher(String name) {
        return publisherRepository.save(new Publisher(UUID.randomUUID(), name));
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
