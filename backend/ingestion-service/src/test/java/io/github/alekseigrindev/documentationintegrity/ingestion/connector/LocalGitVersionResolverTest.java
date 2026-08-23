package io.github.alekseigrindev.documentationintegrity.ingestion.connector;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LocalGitVersionResolverTest {

    private final LocalGitVersionResolver resolver = new LocalGitVersionResolver();

    @Test
    void resolvesCommitForAnUnmodifiedTrackedDocument() {
        Path repositoryRoot = repositoryRoot();
        Path document = repositoryRoot.resolve(
                "backend/ingestion-service/src/test/resources/fixtures/"
                        + "github-docs/content/actions/workflow-permissions.md"
        );

        assertThat(resolver.resolve(repositoryRoot, document))
                .hasValueSatisfying(version ->
                        assertThat(version).matches("[0-9a-f]{40}"));
    }

    private Path repositoryRoot() {
        Path candidate = Path.of("").toAbsolutePath().normalize();

        while (candidate != null && !Files.exists(candidate.resolve(".git"))) {
            candidate = candidate.getParent();
        }

        if (candidate == null) {
            throw new IllegalStateException("Repository root was not found.");
        }

        return candidate;
    }
}
