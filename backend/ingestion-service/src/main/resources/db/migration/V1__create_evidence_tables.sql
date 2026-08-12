CREATE TABLE sources
(
    id                UUID PRIMARY KEY,
    name              TEXT NOT NULL,
    authority_url     TEXT NOT NULL,
    license_name      TEXT NOT NULL,
    license_url       TEXT NOT NULL,
    access_policy_url TEXT NOT NULL,
    CONSTRAINT source_authority_url_key UNIQUE (authority_url)
);

CREATE TABLE source_revisions
(
    id                 UUID PRIMARY KEY,
    source_id          UUID        NOT NULL REFERENCES sources (id),
    version_identifier TEXT        NOT NULL,
    acquisition_method TEXT        NOT NULL,
    acquired_at        TIMESTAMPTZ NOT NULL,
    integrity_hash     VARCHAR(64) NOT NULL,
    CONSTRAINT source_revision_source_version_key UNIQUE (source_id, version_identifier)
);

CREATE TABLE documents
(
    id                 UUID PRIMARY KEY,
    source_revision_id UUID        NOT NULL REFERENCES source_revisions (id),
    source_locator     TEXT        NOT NULL,
    canonical_url      TEXT        NOT NULL,
    product_variant    TEXT        NOT NULL,
    content_hash       VARCHAR(64) NOT NULL,
    attribution        TEXT        NOT NULL,
    resolved_content   TEXT        NOT NULL,
    CONSTRAINT document_revision_locator_key UNIQUE (source_revision_id, source_locator)
);

CREATE TABLE chunks
(
    id           UUID PRIMARY KEY,
    document_id  UUID        NOT NULL REFERENCES documents (id),
    ordinal      INTEGER     NOT NULL CHECK (ordinal >= 0),
    content      TEXT        NOT NULL,
    content_hash VARCHAR(64) NOT NULL,
    CONSTRAINT chunk_document_ordinal_key UNIQUE (document_id, ordinal)
);