ALTER TABLE knowledge.chunks
    ADD COLUMN search_vector TSVECTOR
        GENERATED ALWAYS AS (
            to_tsvector('english'::regconfig, content)
            ) STORED;

CREATE INDEX chunks_search_vector_gin_idx
    ON knowledge.chunks
        USING GIN (search_vector);

COMMENT ON TABLE knowledge.documents IS 'Resolved documentation files imported from immutable source revisions.';
COMMENT ON COLUMN knowledge.documents.id IS 'Stable identifier of the imported documentation file.';
COMMENT ON COLUMN knowledge.documents.source_revision_id IS 'Immutable source revision from which the file was imported.';
COMMENT ON COLUMN knowledge.documents.source_locator IS 'Relative repository path used to locate the imported source file.';
COMMENT ON COLUMN knowledge.documents.canonical_url IS 'Public canonical URL used to cite the documentation page.';
COMMENT ON COLUMN knowledge.documents.product_variant IS 'Documentation product variant represented by the imported file.';
COMMENT ON COLUMN knowledge.documents.content_hash IS 'SHA-256 hash of the resolved documentation content.';
COMMENT ON COLUMN knowledge.documents.attribution IS 'Required attribution text associated with the documentation source.';
COMMENT ON COLUMN knowledge.documents.resolved_content IS 'Documentation content after supported source templates have been resolved.';

COMMENT ON TABLE knowledge.chunks IS 'Deterministic searchable passages derived from imported documentation files.';
COMMENT ON COLUMN knowledge.chunks.id IS 'Stable identifier of the searchable documentation passage.';
COMMENT ON COLUMN knowledge.chunks.document_id IS 'Imported documentation file from which this passage was derived.';
COMMENT ON COLUMN knowledge.chunks.ordinal IS 'Zero-based position of the passage within its source document.';
COMMENT ON COLUMN knowledge.chunks.content IS 'Exact searchable text of the documentation passage.';
COMMENT ON COLUMN knowledge.chunks.content_hash IS 'SHA-256 hash of the exact passage text.';
COMMENT ON COLUMN knowledge.chunks.search_vector IS 'Generated English full-text search representation of the passage content.';