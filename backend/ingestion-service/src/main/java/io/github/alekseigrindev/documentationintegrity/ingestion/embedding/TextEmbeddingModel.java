package io.github.alekseigrindev.documentationintegrity.ingestion.embedding;

import java.util.List;

public interface TextEmbeddingModel {

    List<float[]> embedDocuments(List<String> texts);

    float[] embedQuery(String query);
}