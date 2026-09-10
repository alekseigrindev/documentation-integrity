ALTER TABLE knowledge.ingestion_runs
    ADD COLUMN documents_added BIGINT,
    ADD COLUMN documents_updated BIGINT,
    ADD COLUMN documents_removed BIGINT;
