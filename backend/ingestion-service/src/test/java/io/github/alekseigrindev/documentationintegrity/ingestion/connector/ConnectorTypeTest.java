package io.github.alekseigrindev.documentationintegrity.ingestion.connector;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConnectorTypeTest {

    @Test
    void localDirectoryHasAStableApiAlias() {
        assertThat(ConnectorType.fromAlias("local-directory"))
                .isEqualTo(ConnectorType.LOCAL_DIRECTORY);

        assertThat(ConnectorType.LOCAL_DIRECTORY.toJson())
                .isEqualTo("local-directory");
    }

}