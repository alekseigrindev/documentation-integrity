package io.github.alekseigrindev.documentationintegrity.ingestion.reranking;

import java.util.List;

public interface TextRerankingModel {

    float[] score(List<RerankingInput> inputs);

}
