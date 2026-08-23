package io.github.alekseigrindev.documentationintegrity.ingestion;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves that later migrations preserve data created by the revision-shaped schema.
 */
@Testcontainers
class CurrentStateMigrationTest {

    private static final String SOURCE_ID =
            "11111111-1111-1111-1111-111111111111";
    private static final String REVISION_ID =
            "22222222-2222-2222-2222-222222222222";
    private static final String NEWER_REVISION_ID =
            "22222222-2222-2222-2222-222222222223";
    private static final String DOCUMENT_ID =
            "33333333-3333-3333-3333-333333333333";
    private static final String NEWER_DOCUMENT_ID =
            "33333333-3333-3333-3333-333333333334";

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Test
    void migrationsPreserveCurrentDocumentProvenanceAndLinkItsSource()
            throws Exception {
        flywayAt("3").migrate();

        try (Connection connection = connection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                INSERT INTO knowledge.sources (
                    id, name, authority_url, license_name, license_url,
                    access_policy_url
                ) VALUES (
                    '%s', 'Fixture Docs', 'https://example.invalid/docs',
                    'Apache License 2.0',
                    'https://www.apache.org/licenses/LICENSE-2.0',
                    'https://example.invalid/access-policy'
                )
                """.formatted(SOURCE_ID));
            statement.executeUpdate("""
                INSERT INTO knowledge.source_revisions (
                    id, source_id, version_identifier, acquisition_method,
                    acquired_at
                ) VALUES (
                    '%s', '%s', 'abc123', 'LOCAL_CHECKOUT',
                    '2026-08-20T00:00:00Z'
                )
                """.formatted(REVISION_ID, SOURCE_ID));
            statement.executeUpdate("""
                INSERT INTO knowledge.source_revisions (
                    id, source_id, version_identifier, acquisition_method,
                    acquired_at
                ) VALUES (
                    '%s', '%s', 'def456', 'LOCAL_CHECKOUT',
                    '2026-08-20T01:00:00Z'
                )
                """.formatted(NEWER_REVISION_ID, SOURCE_ID));
            statement.executeUpdate("""
                INSERT INTO knowledge.documents (
                    id, source_revision_id, source_locator, canonical_url,
                    product_variant, content_hash, attribution,
                    resolved_content
                ) VALUES (
                    '%s', '%s', 'guides/test.md',
                    'https://example.invalid/docs/test', 'fixture',
                    'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa',
                    'Fixture attribution', 'Searchable fixture content.'
                )
                """.formatted(DOCUMENT_ID, REVISION_ID));
            statement.executeUpdate("""
                INSERT INTO knowledge.documents (
                    id, source_revision_id, source_locator, canonical_url,
                    product_variant, content_hash, attribution,
                    resolved_content
                ) VALUES (
                    '%s', '%s', 'guides/test.md',
                    'https://example.invalid/docs/test', 'fixture',
                    'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb',
                    'Fixture attribution', 'New searchable fixture content.'
                )
                """.formatted(NEWER_DOCUMENT_ID, NEWER_REVISION_ID));
            statement.executeUpdate("""
                INSERT INTO knowledge.chunks (
                    id, document_id, ordinal, content, content_hash
                ) VALUES (
                    '44444444-4444-4444-4444-444444444444', '%s', 0,
                    'Old searchable fixture content.',
                    'cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc'
                ), (
                    '44444444-4444-4444-4444-444444444445', '%s', 0,
                    'New searchable fixture content.',
                    'dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd'
                )
                """.formatted(DOCUMENT_ID, NEWER_DOCUMENT_ID));
        }

        flywayAt(null).migrate();

        try (Connection connection = connection();
             Statement statement = connection.createStatement()) {
            ResultSet document = statement.executeQuery("""
                SELECT source_id, upstream_version, media_type, acquired_at
                FROM knowledge.documents
                WHERE source_id = '%s'
                  AND product_variant = 'fixture'
                  AND source_locator = 'guides/test.md'
                """.formatted(SOURCE_ID));

            assertThat(document.next()).isTrue();
            assertThat(document.getString("source_id")).isEqualTo(SOURCE_ID);
            assertThat(document.getString("upstream_version")).isEqualTo("def456");
            assertThat(document.getString("media_type")).isEqualTo("text/markdown");
            assertThat(document.getObject("acquired_at")).isNotNull();
            assertThat(document.next()).isFalse();

            ResultSet chunks = statement.executeQuery("""
                SELECT content
                FROM knowledge.chunks
                """);
            assertThat(chunks.next()).isTrue();
            assertThat(chunks.getString("content"))
                    .isEqualTo("New searchable fixture content.");
            assertThat(chunks.next()).isFalse();

            ResultSet migratedSource = statement.executeQuery("""
                SELECT publisher_id, connector_type, source_key, source_url
                FROM knowledge.sources
                WHERE id = '%s'
                """.formatted(SOURCE_ID));
            assertThat(migratedSource.next()).isTrue();
            assertThat(migratedSource.getObject("publisher_id")).isNotNull();
            assertThat(migratedSource.getString("connector_type"))
                    .isEqualTo("GITHUB");
            assertThat(migratedSource.getString("source_key"))
                    .isEqualTo("legacy-" + SOURCE_ID);
            assertThat(migratedSource.getString("source_url"))
                    .isEqualTo("https://example.invalid/docs");

            ResultSet revisionTable = statement.executeQuery(
                    "SELECT to_regclass('knowledge.source_revisions')"
            );
            assertThat(revisionTable.next()).isTrue();
            assertThat(revisionTable.getString(1)).isNull();
        }
    }

    private Flyway flywayAt(String target) {
        var configuration = Flyway.configure()
                .dataSource(
                        postgres.getJdbcUrl(),
                        postgres.getUsername(),
                        postgres.getPassword()
                )
                .schemas("knowledge")
                .defaultSchema("knowledge")
                .createSchemas(true);

        if (target != null) {
            configuration.target(target);
        }

        return configuration.load();
    }

    private Connection connection() throws Exception {
        return DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );
    }
}
