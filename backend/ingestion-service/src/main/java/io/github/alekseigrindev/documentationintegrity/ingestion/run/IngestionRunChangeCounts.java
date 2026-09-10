package io.github.alekseigrindev.documentationintegrity.ingestion.run;

public record IngestionRunChangeCounts(
        long documentsAdded,
        long documentsUpdated,
        long documentsRemoved
) {
}
