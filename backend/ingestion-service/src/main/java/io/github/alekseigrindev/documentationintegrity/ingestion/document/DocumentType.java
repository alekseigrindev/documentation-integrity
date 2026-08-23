package io.github.alekseigrindev.documentationintegrity.ingestion.document;

import lombok.Getter;

@Getter
public enum DocumentType {
    MARKDOWN(".md", "text/markdown");

    private final String fileExtension;
    private final String mimeType;

    private DocumentType(String fileExtension, String mimeType) {
        this.fileExtension = fileExtension;
        this.mimeType = mimeType;
    }
}
