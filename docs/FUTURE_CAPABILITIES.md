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

**Why deferred:** The current Publisher slice only creates publisher identity.
Designing storage and restoration semantics before a deletion workflow exists
would add state with no enforceable meaning.

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

## Corpus Snapshots and Atomic Rollback

**User problem:** Search must never mix documents from different acquisition
runs, or an operator needs to activate and roll back a complete corpus as one
unit.

**Implementation trigger:** Ingestion and search run concurrently, multiple
operators update one source, or a measured rollback requirement appears.

**Why deferred:** v1 ingestion and search are manually sequenced. Snapshot
lifecycle and rollback machinery would not change the current user outcome.

## Historical Document Versions

**User problem:** Operators need local change comparison, answer-impact
analysis, or contradiction detection without reacquiring old source content.

**Implementation trigger:** Reconstructing previous content from the upstream
source is unavailable or fails a measured latency or reliability requirement.

**Why deferred:** The current product needs the latest searchable document
state. Retaining every derived version would duplicate upstream history before
historical comparison is implemented.

## Advanced Ingestion Run Operations

**User problem:** An operator needs reliable recovery and usable history when
ingestion runs become numerous, long-lived, or asynchronous.

**Implementation trigger:** Measured run history requires pagination, an
interrupted worker leaves stale state, or an accepted asynchronous boundary
requires explicit recovery semantics.

**Why deferred:** Current imports are synchronous and manually initiated. The
advanced lifecycle and storage constraints are selected only after those
behaviors are demonstrated.

## Source and Ingestion Run Document-Type Selection

**User problem:** A source scope can contain Markdown, PDF, DOCX, images, and
other files, but an operator needs predictable control over which approved
formats are scanned in a particular synchronization run.

**Implementation trigger:** A source-wide connector supports at least two
document processors and has an executable scan fixture containing mixed file
types.

**Why deferred:** V1 intentionally supports only Markdown and plain text.
Designing persisted selections and request overrides before a second processor
exists would create configuration without executable behavior.
