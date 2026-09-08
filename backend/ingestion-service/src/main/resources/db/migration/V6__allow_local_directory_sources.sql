ALTER TABLE knowledge.sources
    DROP CONSTRAINT sources_connector_type_check,
    ADD CONSTRAINT sources_connector_type_check
        CHECK (connector_type IN ('GITHUB', 'LOCAL_DIRECTORY'));
