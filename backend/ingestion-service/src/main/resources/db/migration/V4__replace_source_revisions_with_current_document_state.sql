ALTER TABLE knowledge.documents
    ADD COLUMN source_id UUID,
    ADD COLUMN upstream_version TEXT,
    ADD COLUMN media_type TEXT,
    ADD COLUMN acquired_at TIMESTAMPTZ;

UPDATE knowledge.documents AS document
SET source_id = revision.source_id,
    upstream_version = revision.version_identifier,
    media_type = 'text/markdown',
    acquired_at = revision.acquired_at
FROM knowledge.source_revisions AS revision
WHERE revision.id = document.source_revision_id;

WITH duplicate_documents AS (
    SELECT id
    FROM (
        SELECT id,
               ROW_NUMBER() OVER (
                   PARTITION BY source_id, product_variant, source_locator
                   ORDER BY acquired_at DESC, id DESC
               ) AS duplicate_rank
        FROM knowledge.documents
    ) AS ranked_documents
    WHERE duplicate_rank > 1
)
DELETE FROM knowledge.chunks AS chunk
USING duplicate_documents
WHERE chunk.document_id = duplicate_documents.id;

WITH duplicate_documents AS (
    SELECT id
    FROM (
        SELECT id,
               ROW_NUMBER() OVER (
                   PARTITION BY source_id, product_variant, source_locator
                   ORDER BY acquired_at DESC, id DESC
               ) AS duplicate_rank
        FROM knowledge.documents
    ) AS ranked_documents
    WHERE duplicate_rank > 1
)
DELETE FROM knowledge.documents AS document
USING duplicate_documents
WHERE document.id = duplicate_documents.id;

ALTER TABLE knowledge.documents
    ALTER COLUMN source_id SET NOT NULL,
    ALTER COLUMN media_type SET NOT NULL,
    ALTER COLUMN acquired_at SET NOT NULL,
    ALTER COLUMN canonical_url DROP NOT NULL,
    ADD CONSTRAINT document_source_id_fkey
        FOREIGN KEY (source_id) REFERENCES knowledge.sources (id),
    DROP CONSTRAINT document_revision_locator_key,
    ADD CONSTRAINT document_source_variant_locator_key
        UNIQUE (source_id, product_variant, source_locator),
    DROP CONSTRAINT documents_source_revision_id_fkey,
    DROP COLUMN source_revision_id;

ALTER TABLE knowledge.chunks
    DROP CONSTRAINT chunks_document_id_fkey,
    ADD CONSTRAINT chunks_document_id_fkey
        FOREIGN KEY (document_id) REFERENCES knowledge.documents (id)
            ON DELETE CASCADE;

DROP TABLE knowledge.source_revisions;

CREATE TABLE knowledge.ingestion_runs
(
    id                  UUID PRIMARY KEY,
    source_id           UUID        NOT NULL REFERENCES knowledge.sources (id),
    status              TEXT        NOT NULL,
    started_at          TIMESTAMPTZ NOT NULL,
    finished_at         TIMESTAMPTZ,
    failure_code        TEXT,
    failure_message     TEXT
);

CREATE INDEX ingestion_runs_source_started_idx
    ON knowledge.ingestion_runs (source_id, started_at DESC, id DESC);

COMMENT ON TABLE knowledge.documents IS 'Latest successfully ingested state of each logical documentation document.';
COMMENT ON COLUMN knowledge.documents.source_id IS 'Approved documentation source that owns the logical document.';
COMMENT ON COLUMN knowledge.documents.source_locator IS 'Stable connector-relative identity of the document within its source.';
COMMENT ON COLUMN knowledge.documents.canonical_url IS 'Optional public URL used to cite the document.';
COMMENT ON COLUMN knowledge.documents.product_variant IS 'Product or documentation variant represented by the document.';
COMMENT ON COLUMN knowledge.documents.upstream_version IS 'Optional connector-provided document version, such as a Git commit SHA.';
COMMENT ON COLUMN knowledge.documents.media_type IS 'Normalized media type of the acquired document.';
COMMENT ON COLUMN knowledge.documents.acquired_at IS 'Server timestamp when this document state was acquired.';
COMMENT ON COLUMN knowledge.documents.content_hash IS 'SHA-256 hash of the normalized document content.';
COMMENT ON COLUMN knowledge.documents.attribution IS 'Attribution required by the approved source.';
COMMENT ON COLUMN knowledge.documents.resolved_content IS 'Normalized content used to derive searchable chunks.';

COMMENT ON TABLE knowledge.ingestion_runs IS 'Observable attempts to synchronize one documentation source.';
COMMENT ON COLUMN knowledge.ingestion_runs.source_id IS 'Documentation source targeted by the run.';
COMMENT ON COLUMN knowledge.ingestion_runs.status IS 'Current lifecycle state of the run.';
COMMENT ON COLUMN knowledge.ingestion_runs.started_at IS 'Server timestamp when source synchronization started.';
COMMENT ON COLUMN knowledge.ingestion_runs.finished_at IS 'Server timestamp when the run reached a terminal state.';
COMMENT ON COLUMN knowledge.ingestion_runs.failure_code IS 'Stable failure category when the run fails.';
COMMENT ON COLUMN knowledge.ingestion_runs.failure_message IS 'Bounded diagnostic message for an operator.';
