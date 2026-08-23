# Future Capabilities

This document records plausible extensions that are deliberately outside the
current implementation scope. An entry is not a delivery promise or a claim
that the capability exists.

Each capability must enter a milestone only after its user problem and
implementation trigger are demonstrated.

## Publisher Soft Deletion

**User problem:** An operator must be able to revoke a publisher without
removing its identity or breaking historical references from documentation
sources and ingestion evidence.

**Implementation trigger:** Publisher deletion is exposed as an operator
workflow and the behavior of active sources owned by that publisher is defined.

**Expected behavior:** Add an `is_deleted` flag with a default value of
`false`. Soft-deleted publishers are excluded from normal active-publisher
queries and cannot be assigned to new sources. Before implementation, define
whether existing sources are blocked, disabled, or remain readable, and define
how publisher-name uniqueness and restoration behave after deletion.

**Why deferred:** The current Publisher slice only creates publisher identity.
Adding an unused flag before deletion and source-reference behavior exist would
store state with no enforceable meaning.

## Authenticated Jira Connector

**User problem:** Teams need issue-based operational knowledge to participate
in the same evidence-preserving retrieval pipeline as documentation.

**Implementation trigger:** A concrete demonstration corpus and permission
model are available without exposing proprietary material or credentials.

**Why deferred:** OAuth or tokens, project permissions, pagination, rate
limits, comments, attachments, deletion, and retention require explicit
contracts and public-safe fixtures.

## Authenticated Google Docs Connector

**User problem:** Teams need collaboratively maintained documents to
participate in retrieval and quality evaluation.

**Implementation trigger:** A concrete authorized corpus and testable export,
permission, deletion, and attribution semantics are selected.

**Why deferred:** Authentication, shared-drive permissions, rich document
rendering, revision APIs, quotas, and proprietary-content handling would expand
v1 without improving the initial retrieval baseline.

## Source-Specific Revisions

**User problem:** A connector exposes a meaningful upstream-wide version that
must be queried, audited, or compared as a domain object.

**Implementation trigger:** A real connector requires behavior that cannot be
represented by optional document-level upstream-version metadata.

**Why deferred:** A mandatory `SourceRevision` imposes Git-like global
versioning on sources whose documents change independently.

## Corpus Snapshots

**User problem:** Search must never mix documents from different acquisition
runs, or an operator needs atomic corpus rollback.

**Implementation trigger:** Ingestion and search run concurrently, multiple
operators update one source, or a measured rollback requirement appears.

**Why deferred:** v1 ingestion and search are manually sequenced. `STAGING`,
`ACTIVE`, and `RETIRED` state machinery would add storage, query filtering, and
lifecycle complexity without changing the current user outcome.

## Historical Document Versions

**User problem:** Operators need local change comparison, answer-impact
analysis, or contradiction detection without reacquiring old source content.

**Implementation trigger:** Reconstructing previous content from the upstream
source is unavailable or fails a measured latency or reliability requirement.

**Why deferred:** The current product needs the latest searchable document
state. Retaining every derived version would duplicate upstream history before
historical comparison is implemented.

## Ingestion Run Operations

**User problem:** An operator needs a reliable, safe history of imports when
sources contain many documents or ingestion becomes asynchronous.

**Implementation trigger:** A connector imports more than one document per
operation, run history becomes large enough to impede inspection, or Kafka
introduces worker interruption and delayed delivery.

**Expected behavior:** Expire or explicitly fail stale `RUNNING` records using
a documented worker lease or timeout; expose run history through cursor-based
pagination; and return only sanitized operator diagnostics. Raw exception
details, filesystem paths, signed URLs, and credentials must not be persisted
or exposed through the run API.

**Why deferred:** Current imports are synchronous and manually initiated. The
existing source-level audit fields identify each attempt without requiring
background recovery machinery or a paginated operational UI.

## Ingestion Run Lifecycle Constraints

**User problem:** Operators and automated workers need a single, proven
definition of valid ingestion-run states and their permitted transitions.

**Implementation trigger:** A source-wide connector, Kafka consumer, retry
policy, or stale-run recovery workflow demonstrates the required lifecycle and
the semantics of partial failure.

**Expected behavior:** Define the lifecycle states, terminal-state fields,
failure-code vocabulary, and transition rules. Add PostgreSQL `CHECK`
constraints only after the application behavior and operational recovery model
are covered by integration tests.

**Why deferred:** The current MVP has no validated source-wide run lifecycle.
Database constraints would freeze assumptions about status values and failure
semantics before real connectors establish the correct contract.

## Source and Ingestion Run Document-Type Selection

**User problem:** A source scope can contain Markdown, PDF, DOCX, images, and
other files, but an operator needs predictable control over which approved
formats are scanned in a particular synchronization run.

**Implementation trigger:** A source-wide connector supports at least two
document processors and has an executable scan fixture containing mixed file
types.

**Expected behavior:** Store a source-level default allowlist of
`DocumentType` values. A future `POST /api/admin/ingestion-runs` request may
provide an optional document-type selection. When omitted, the run uses the
source default; when provided, it replaces that default for the run and may be
narrower or broader. Every requested type must be supported by an installed
document processor. Persist the effective type selection with the run so
operational history explains its scope. A run never mutates source defaults.

**Why deferred:** The MVP supports Markdown only and does not yet have a
source-wide scan or a second document processor. Adding allowlist persistence
and request overrides now would create configuration without executable
behavior.

## Atomic Snapshot Activation and Rollback

**User problem:** A complete new corpus must become visible at once, and the
previous corpus must remain immediately recoverable.

**Implementation trigger:** Concurrent production search during ingestion or a
bounded recovery-time requirement makes per-document replacement insufficient.

**Why deferred:** The v1 operator starts search only after manual ingestion has
completed and been verified.
