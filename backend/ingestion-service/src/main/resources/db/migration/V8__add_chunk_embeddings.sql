CREATE EXTENSION IF NOT EXISTS vector;

ALTER TABLE knowledge.chunks
    ADD COLUMN embedding VECTOR(768);

CREATE INDEX chunks_embedding_hnsw_idx
    ON knowledge.chunks
    USING HNSW (embedding vector_cosine_ops);

COMMENT ON COLUMN knowledge.chunks.embedding IS
    'L2-normalized 768-dimensional Nomic embedding of the searchable chunk content.';