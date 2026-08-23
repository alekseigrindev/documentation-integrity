CREATE TABLE knowledge.publishers
(
    id   UUID PRIMARY KEY,
    name TEXT NOT NULL,
    CONSTRAINT publisher_name_key UNIQUE (name)
);

ALTER TABLE knowledge.sources
    RENAME COLUMN authority_url TO source_url;

ALTER TABLE knowledge.sources
    DROP CONSTRAINT source_authority_url_key,
    ALTER COLUMN source_url DROP NOT NULL,
    ALTER COLUMN license_name DROP NOT NULL,
    ALTER COLUMN license_url DROP NOT NULL,
    ALTER COLUMN access_policy_url DROP NOT NULL,
    ADD COLUMN publisher_id UUID,
    ADD COLUMN connector_type TEXT,
    ADD COLUMN source_key TEXT,
    ADD COLUMN description TEXT;

INSERT INTO knowledge.publishers (id, name)
SELECT DISTINCT ON (source.name)
       source.id,
       source.name
FROM knowledge.sources AS source
ORDER BY source.name, source.id
ON CONFLICT (name) DO NOTHING;

UPDATE knowledge.sources AS source
SET publisher_id = publisher.id,
    connector_type = 'GITHUB',
    source_key = 'legacy-' || source.id::text
FROM knowledge.publishers AS publisher
WHERE publisher.name = source.name;

ALTER TABLE knowledge.sources
    ALTER COLUMN publisher_id SET NOT NULL,
    ALTER COLUMN connector_type SET NOT NULL,
    ALTER COLUMN source_key SET NOT NULL,
    ADD CONSTRAINT sources_publisher_id_fkey
        FOREIGN KEY (publisher_id) REFERENCES knowledge.publishers (id),
    ADD CONSTRAINT sources_connector_type_check
        CHECK (connector_type IN ('GITHUB')),
    ADD CONSTRAINT source_source_key_key UNIQUE (source_key);

CREATE INDEX sources_publisher_id_idx
    ON knowledge.sources (publisher_id);

COMMENT ON TABLE knowledge.publishers IS 'Organizations or teams responsible for approved documentation.';
COMMENT ON COLUMN knowledge.publishers.id IS 'Server-generated publisher identity.';
COMMENT ON COLUMN knowledge.publishers.name IS 'Unique operator-facing publisher name.';

COMMENT ON TABLE knowledge.sources IS
    'Approved logical documentation sources owned by publishers and acquired through supported connectors.';
COMMENT ON COLUMN knowledge.sources.publisher_id IS
    'Publisher responsible for the documentation exposed by this source.';
COMMENT ON COLUMN knowledge.sources.connector_type IS
    'Implemented platform connector used to acquire documents from this source.';
COMMENT ON COLUMN knowledge.sources.source_key IS
    'Stable unique identifier for the logical documentation source, independent of acquisition location.';
COMMENT ON COLUMN knowledge.sources.description IS
    'Optional operator-facing context about the source; it is not indexed as documentation content.';
COMMENT ON COLUMN knowledge.sources.source_url IS
    'Optional public or connector-relevant URL describing where the source is available.';
COMMENT ON COLUMN knowledge.sources.license_name IS
    'Optional license name supplied when known for the source.';
COMMENT ON COLUMN knowledge.sources.license_url IS
    'Optional URL for the source license.';
COMMENT ON COLUMN knowledge.sources.access_policy_url IS
    'Optional URL describing access or retention policy for the source.';
