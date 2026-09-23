package io.github.alekseigrindev.documentationintegrity.ingestion.search;

import lombok.Getter;

@Getter
public enum RetrievalMethod {
    LEXICAL("Lexical search"),
    VECTOR("Vector search"),
    HYBRID("Hybrid search");

    private final String displayName;

    RetrievalMethod(String displayName) {
        this.displayName = displayName;
    }

}
