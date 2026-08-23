package io.github.alekseigrindev.documentationintegrity.ingestion.publisher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PublisherAdminControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PublisherRepository publisherRepository;

    @BeforeEach
    void clearPublishers() {
        publisherRepository.deleteAll();
    }

    @Test
    void createsAndListsPublishersInStableOrder() throws Exception {
        createPublisher("Zulu Documentation");

        mockMvc.perform(post("/api/admin/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"  Alpha Documentation  "}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Alpha Documentation"));

        mockMvc.perform(get("/api/admin/publishers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Alpha Documentation"))
                .andExpect(jsonPath("$[1].name").value("Zulu Documentation"));

        assertThat(publisherRepository.count()).isEqualTo(2);
    }

    @Test
    void registeringTheSamePublisherTwiceReturnsTheExistingPublisher()
            throws Exception {
        createPublisher("GitHub Inc.");

        mockMvc.perform(post("/api/admin/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"GitHub Inc."}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("GitHub Inc."));

        assertThat(publisherRepository.count()).isEqualTo(1);
    }

    @Test
    void rejectsBlankPublisherName() throws Exception {
        mockMvc.perform(post("/api/admin/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":" "}
                                """))
                .andExpect(status().isBadRequest());

        assertThat(publisherRepository.count()).isZero();
    }

    private void createPublisher(String name) throws Exception {
        mockMvc.perform(post("/api/admin/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s"}
                                """.formatted(name)))
                .andExpect(status().isCreated());
    }
}
