# Future Capabilities

This document records plausible extensions that are deliberately outside the
current implementation scope. An entry is not a delivery promise or a claim
that the capability exists.

Each capability must enter a milestone only after its user problem and
implementation trigger are demonstrated.

## High-Priority Future Capabilities

### Candidate M10 — Bounded Synchronization Execution

**User problem:** Starting synchronization for several Sources currently runs
all of them immediately. Embedding or reranking work can then compete for CPU,
memory, native-runtime threads, and database connections, making the host
unresponsive and performance measurements non-representative.

**Required behavior:** Synchronization requests enter a bounded queue when all
worker capacity is occupied. A configurable global concurrency limit controls
how many runs may execute, and one Source cannot have overlapping queued or
running synchronization. Operators can distinguish queued, running, succeeded,
and failed runs and can see bounded progress without document content appearing
in logs. A full queue produces an explicit rejection or backpressure result
rather than unbounded memory growth. Performance reports record the worker
limit, embedding batch size, model runtime settings, and host resources used by
the measurement.

**Implementation trigger:** Embedding and reranking establish the actual CPU,
memory, and latency profile of one synchronization, and concurrent Source runs
demonstrably contend for the same host resources.

**Why deferred:** M8 must first prove correct vector retrieval and obtain a
sequential baseline; reranking must also expose its measured resource profile.
The initial solution should be the smallest bounded in-process queue that meets
the accepted recovery semantics. Kafka or another broker requires separate
evidence that durable cross-process delivery is needed.

### Browser Directory Upload for Remote Ingestion

**User problem:** A user accessing the web application from their own computer
must be able to select a local directory and ingest its supported files when
`ingestion-service` runs on a remote host or VPS.

**Required behavior:** The browser receives the user's explicit directory
selection, filters selected files by supported format and size for fast
feedback, preserves relative paths, and uploads only eligible file contents.
The backend repeats all validation and owns ingestion, provenance, chunking,
storage, and failure handling. The server never receives unrestricted access
to the user's local filesystem or a local absolute path.

**Implementation trigger:** A remote or VPS deployment is selected and an
operator must ingest documentation from their own local computer.

**Why deferred:** This is the file-upload connector flow, not an extension of
the same-host local-directory picker. It requires an upload API, size/count
limits, multipart or streaming behavior, relative-path semantics, and
integration evidence. M6 currently measures retrieval quality.

## Retrieval Method Activation

**User problem:** After two or more retrieval configurations have measured
results, an operator needs to activate a validated configuration or return to
the lexical baseline without changing application code.

**Required behavior:** An operator-facing setting lists only evaluated retrieval
configurations, identifies the active configuration, and permits a safe switch
to another validated configuration. Retrieval methods remain application
strategies by default; enabling or disabling one does not imply starting or
stopping a separately deployed service.

**Implementation trigger:** At least two retrieval configurations have
reproducible comparison reports and an operator needs to switch the live method
without a code deployment.

**Why deferred:** M7 first proves one browser-to-result lexical evaluation
flow. A settings UI before a second measured method exists would create empty
configuration. Managing separately deployed services would additionally require
authentication, validation, health checks, audit history, and rollback rules.

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
