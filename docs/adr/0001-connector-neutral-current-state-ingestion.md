# ADR 0001: Connector-Neutral Current-State Ingestion

- Status: Accepted; core transition implemented
- Date: 2026-08-20

## Context

The first ingestion slice modeled Git acquisition with a mandatory
`SourceRevision`. That model associates documents with one source-wide version
before they can be imported.

The product direction includes sources whose documents may change
independently. Jira issues, Google documents, and uploaded files do not share
one universal source-revision semantic. Keeping `SourceRevision` in the core
would either couple the domain to Git or require artificial revision values.

The v1 operator runs ingestion manually and starts search after ingestion has
completed. Concurrent corpus activation, historical comparison, and immediate
rollback are not current user requirements.

## Decision

The v1 target model stores one current successfully ingested representation of
each logical document.

A logical document is identified by source, product variant, and stable source
locator. Canonical URL and upstream version are optional provenance fields.
For a Git connector, the upstream version is a commit SHA; other connectors may
use their own revision identity or leave it absent.

Source-specific acquisition is implemented behind a connector-neutral contract
that emits normalized acquired documents. The v1 contract is proven with a
local-directory connector and a controlled Markdown or text file-upload
connector.

`IngestionRun` records operational status, timing, and failure details for one
source synchronization attempt. A failed run may leave partially synchronized
current document state; the v1 operator runs search only after a successful
source run.

On re-ingestion:

- a missing logical document is inserted;
- an unchanged content hash is skipped;
- a changed document has its content and derived chunks and embeddings replaced
  transactionally after those replacements are prepared;
- documents absent from a successful complete source scan are deleted;
- failed or partial scans do not trigger stale-document deletion.

`SourceRevision`, `CorpusSnapshot`, retained historical document versions, and
atomic snapshot activation are outside the v1 target model.

## Alternatives Considered

### Mandatory Source Revisions

This preserves a natural Git commit boundary and avoids repeating commit
metadata. It was rejected for the v1 target because it imposes global version
semantics on connectors whose documents change independently.

### Corpus Snapshots With Staging and Activation

This prevents search from observing a corpus assembled across ingestion runs
and enables rollback. It was rejected because v1 ingestion and search are
manually sequenced, so the additional lifecycle, storage, and query filtering
do not change the current user outcome.

### Connector-Specific Persistence Models

This can preserve every upstream system's native concepts. It was rejected for
v1 because it duplicates the downstream hashing, chunking, indexing, and
retrieval path before a concrete connector requires specialized behavior.

## Consequences

- The source-revision-shaped API and schema were replaced by a Flyway migration
  that retains the newest current document for each logical identity.
- Connector-specific versions remain available as optional provenance without
  controlling the document lifecycle.
- PostgreSQL stores only current searchable state, reducing storage and query
  complexity.
- Historical comparison requires reacquiring upstream content until a measured
  product requirement justifies local history.
- Complete source scans and stale-document deletion remain pending. Their
  implementation must distinguish success from partial failure before deleting
  stored documents.

## Reconsideration Triggers

Reconsider corpus snapshots or source-specific revision entities when at least
one of these conditions is demonstrated:

- ingestion and search must run concurrently without mixed acquisition states;
- operators require bounded-time corpus rollback;
- an upstream connector exposes a source-wide revision with required behavior;
- freshness, change-impact, or contradiction analysis requires local historical
  comparison;
- reconstructing old content from the upstream source fails a measured latency
  or reliability requirement.

## Required Evidence

- Local-directory and file-upload connectors produce the same normalized
  document contract.
- Re-ingesting unchanged content creates no duplicates.
- Re-ingesting changed content leaves no stale chunks searchable.
- A failed or partial complete scan cannot delete current documents.
- Connector-specific provenance remains present in citations and audit data.
