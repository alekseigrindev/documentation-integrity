package io.github.alekseigrindev.documentationintegrity.ingestion.connector;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ConnectorType {
    GITHUB("github", "For GitHub sources");

    private final String alias;
    private final String description;

    ConnectorType(String alias, String description) {
        this.alias = alias;
        this.description = description;
    }

    @JsonCreator
    public static ConnectorType fromAlias(String alias) {
        if (alias == null) {
            return null;
        }

        return Arrays.stream(values())
                .filter(type -> type.alias.equalsIgnoreCase(alias.strip()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported connector type: " + alias
                ));
    }

    @JsonValue
    public String toJson() {
        return alias;
    }
}
