# Technology Stack Specification — v1.0.0

## Principles

- Evidence before claims: each public capability must be backed by a test,
  metric, screenshot, trace, or reproducible command.
- Minimize operational surface until a boundary has a demonstrated reason to
  scale or fail independently.
- Keep retrieval and evaluation logic testable outside framework-specific
  adapters.
- Treat retrieved documents as untrusted data.
- Ingest only allowlisted sources whose automated access, retention, and use
  are permitted.
- Acquire the initial documentation from its licensed Git repository rather
  than crawling the rendered website.
- Keep source-specific acquisition behind a connector-neutral application
  boundary and store the current successfully indexed document state in v1.

## Delivery Authorization

This document records implemented choices, v1 release targets, and conditional
architecture candidates. It is not an implementation queue. `CURRENT_FOCUS.md`
defines the active milestone and is the authority for work that may begin now.

A target technology enters implementation only when it is required by the
active user or operator outcome, has an acceptance criterion, and can produce
executable evidence. Internal portfolio value alone is not sufficient.

Kafka and gRPC are conditional adoption decisions. Their presence in diagrams,
rationale, or the target stack does not make them v1 completion criteria unless
an active milestone explicitly retains and proves their role.

## Implementation Status

| Area | Choice | Status |
| --- | --- | --- |
| Primary language and runtime | Java 25 | Implemented |
| Application framework | Spring Boot 4.1.0; Spring AI 2.0.0 target | Boot application implemented; Spring AI pending |
| Backend build | Maven reactor | Implemented; Maven Wrapper pending |
| Web client | React + TypeScript | Publisher and Source management implemented; Source synchronization active in M5 |
| Repository layout | `backend/`, `frontend/`, `infra/`, `docs/` | Backend, infrastructure, docs, and frontend foundation present |
| Local infrastructure | Docker Compose | Implemented |
| Transactional/vector storage | PostgreSQL 17 + pgvector | Container verified |
| Schema migrations | Flyway | Implemented and integration-tested |
| Lexical search | PostgreSQL FTS + GIN | Implemented and integration-tested |
| Vector search | pgvector + HNSW | Planned for measurable retrieval; not active |
| Chat model | `qwen3:8b` via host Ollama | Planned for diagnosis; not active |
| Embedding model | `nomic-embed-text` via ONNX Runtime | Planned for measurable retrieval; not active |
| Reranker | `bge-reranker-v2-m3` via ONNX Runtime | Planned after a measured hybrid baseline; not active |
| Async ingestion | Apache Kafka | Container verified; application use is a conditional source-synchronization decision |
| Internal RPC | gRPC + protobuf | Conditional decision after an in-process retrieval boundary is measured |
| Testing | JUnit 5, AssertJ, Testcontainers | Implemented for current ingestion slice |
| Observability | Micrometer + OpenTelemetry | Planned only where milestone acceptance requires measured behavior |
| CI | GitHub Actions | Planned; not active |
| Initial corpus | `github/docs` Actions content + referenced data | Selected; complete processing is planned, not active |
| Source acquisition | Local directory + file upload behind one connector contract | Implemented and integration-tested for single-document synchronization |

## Incremental Architecture

1. Keep the existing executable backend, PostgreSQL/Flyway ownership, lexical
   lookup, and connector-neutral current-state ingestion as the verified
   baseline.
2. Establish a minimal React and TypeScript application shell before adding
   product behavior.
3. Deliver Publisher, Source, and source-synchronization behavior as separate
   vertical slices, each with backend, frontend, tests, and acceptance evidence.
4. Introduce Kafka only if the active synchronization slice requires durable
   asynchronous handoff; ship idempotency, retries, dead-letter behavior, and
   metrics with that decision.
5. Build measurable lexical, vector, hybrid, and reranked retrieval behind an
   in-process typed contract first.
6. Extract a physical retrieval service and introduce protobuf/gRPC only if an
   ADR demonstrates a boundary that must scale or fail independently.
7. Grow the frontend shell into retrieval inspection and diagnostic chat in
   the same milestones as the corresponding backend behavior.

Empty service directories are not considered implemented services.

## Retrieval Pipeline

1. Normalize the problem statement and optional workflow/error excerpt, then
   extract retrieval hints such as topic, identifiers, and explicit product
   variant without inventing missing context.
2. Run PostgreSQL full-text and pgvector searches over the rendered GitHub
   Actions corpus.
3. Fuse ranked candidates using a documented algorithm; Reciprocal Rank Fusion
   is the initial candidate.
4. Rerank the fused candidates with `bge-reranker-v2-m3` in ONNX Runtime.
5. Apply an evidence threshold and abstain when support is insufficient.
6. Generate an evidence-backed diagnosis with `qwen3:8b` through Ollama.
7. Return assumptions, citations, any supported corrective example, and
   stage-level telemetry.

Lexical-only, vector-only, hybrid, and reranked configurations must remain
independently testable for evaluation.

## Technology Rationale and Required Evidence

### PostgreSQL and pgvector

One datastore holds transactional metadata, provenance, lexical indexes, and
vectors. This limits operational complexity at the expected initial scale.

Required evidence: Flyway migrations, query plans, HNSW/GIN indexes, retrieval
benchmarks, and an explicit reconsideration trigger.

### Kafka

Kafka is the selected candidate if source ingestion demonstrates a need for
durable work handoff, replay, backpressure, and failure isolation. The
synchronous source path remains the baseline until that need is proven in the
active milestone.

Required evidence: versioned event contract, idempotency key, partition-key
rationale, retry/DLT policy, duplicate-delivery test, and consumer-lag metric.

### gRPC

gRPC is the selected candidate if retrieval and local inference demonstrate a
typed, latency-sensitive boundary that must scale or fail independently from
the public API. An in-process typed contract is the baseline.

Required evidence: versioned protobuf, deadlines, status mapping, health
behavior, tracing, and compatibility tests. A service split must be justified
against an in-process module and REST.

### ONNX Runtime

Embedding and reranking in the JVM avoid a separate Python inference service and
make the Java execution path reproducible.

Required evidence: tokenizer/model compatibility, checksum and license metadata,
warm-up behavior, memory use, batch-size benchmark, and deterministic fixtures.

### Spring AI

Spring AI provides model abstractions, Ollama integration, and observability
hooks. Retrieval, ranking, and evaluation rules remain application-owned domain
logic rather than framework configuration.

## Data and Model Policy

- Store no third-party corpus or downloaded model in Git.
- Clone or update the official `github/docs` repository and ingest only the
  allowlisted `content/actions/` tree plus referenced `data/reusables/` and
  `data/variables/` content.
- Render the current GitHub.com (`fpt`) variant from Markdown, front matter,
  reusable fragments, variables, and supported Liquid version conditions.
  Unresolved directives are ingestion errors, not searchable text.
- Record source identity, stable source locator, canonical URL when available,
  optional upstream version, product variant, attribution, ingestion time, and
  content hash. For GitHub Docs, record the repository URL, path, commit SHA,
  and CC BY 4.0 metadata.
- Keep one current searchable representation per logical document in v1.
  Generate replacements before opening the database transaction, then replace
  changed document content, chunks, and embeddings transactionally.
- Record source ingestion attempts separately from source content. Each
  attempt owns its status, timing, and failure details; it is not a source
  revision. A failed attempt may leave partial current state, so the MVP
  operator searches only after a successful attempt.
- Do not crawl `docs.github.com` or commit the acquired corpus or derived
  embeddings.
- Record model name, version/revision, checksum, license, quantization, tokenizer,
  and runtime configuration for every benchmark.
- Use synthetic or explicitly licensed fixtures in CI.
- Never commit uploaded documents. Public tests and demonstrations use
  synthetic or explicitly licensed uploads.

## Reliability and Security

- Treat retrieved text as untrusted context that cannot override system
  instructions.
- Treat submitted workflow fragments and logs as untrusted and potentially
  secret-bearing. Do not persist or log their raw content by default.
- Bound source-file size, include-expansion depth, parsing concurrency, and
  supported content types.
- Propagate correlation IDs and deadlines across REST and across Kafka or gRPC
  when those boundaries are introduced.
- Define health/readiness separately from dependency availability.
- If Kafka delivery is introduced, make ingestion safe under retries and
  duplicate delivery.
- Never describe the system as production-ready, scalable, accurate, or secure
  without bounded evidence.

## Testing and Evaluation

- Unit tests for connector normalization, source rendering, request-context
  extraction, chunking, fusion, thresholds, and citation assembly.
- Testcontainers integration tests for PostgreSQL/pgvector and Flyway, plus
  Kafka when Kafka enters the active milestone.
- Contract compatibility tests for public API schemas and for protobuf if a
  gRPC boundary is introduced.
- End-to-end test using a small legally usable fixture corpus.
- Versioned offline evaluation covering retrieval, ranking, citation support,
  abstention, latency, and adversarial document content.

## Open Decisions

1. Exact Spring Boot 4.x patch version.
2. Hosted-demo inference strategy.
3. GitHub Docs rendering strategy for reusables, variables, and version
   conditions.
4. Physical service split trigger for retrieval.
5. Initial evidence threshold and latency target after baseline measurement.
6. Model artifact download and verification mechanism.
7. Input redaction and retention policy for submitted workflows and logs.
8. Authentication and permission model for future Jira and Google Docs
   connectors.
