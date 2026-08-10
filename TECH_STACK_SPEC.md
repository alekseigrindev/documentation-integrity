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

## Implementation Status

| Area | Choice | Status |
| --- | --- | --- |
| Primary language and runtime | Java 25 | Selected; application pending |
| Application framework | Spring Boot 4.x + Spring AI 2.0.0 | Exact Boot patch pending |
| Backend build | Maven reactor + Maven Wrapper | In progress |
| Web client | React + TypeScript | Target |
| Repository layout | `backend/`, `frontend/`, `infra/`, `docs/` | Selected; infrastructure present, application directories pending |
| Local infrastructure | Docker Compose | Implemented |
| Transactional/vector storage | PostgreSQL 17 + pgvector | Container verified |
| Schema migrations | Flyway | Next |
| Lexical search | PostgreSQL FTS + GIN | Target |
| Vector search | pgvector + HNSW | Target |
| Chat model | `qwen3:8b` via host Ollama | Target |
| Embedding model | `nomic-embed-text` via ONNX Runtime | Target |
| Reranker | `bge-reranker-v2-m3` via ONNX Runtime | Target |
| Async ingestion | Apache Kafka | Container verified; application boundary pending |
| Internal RPC | gRPC + protobuf | Target after retrieval boundary exists |
| Testing | JUnit 5, AssertJ, Testcontainers | Next |
| Observability | Micrometer + OpenTelemetry | Target |
| CI | GitHub Actions | Next |
| Initial corpus | `github/docs` Actions content + referenced data | Selected |
| Source acquisition | Pinned Git checkout | Target |

## Incremental Architecture

1. Build one executable `api-service` with health/readiness and a reproducible
   Maven build.
2. Add PostgreSQL ownership through Flyway and Testcontainers.
3. Prove one synchronous ingestion-to-answer vertical slice with a synthetic
   fixture shaped like the selected GitHub Actions corpus.
4. Extract a typed retrieval boundary and introduce protobuf/gRPC when
   independent scaling and latency instrumentation can be demonstrated.
5. Introduce Kafka when ingestion becomes asynchronous; ship idempotency,
   retries, dead-letter handling, and consumer metrics with it.
6. Add the React chat after the public API contract is stable.

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

Ingestion is long-running, retryable, and bursty. Kafka provides durable work
handoff, replay, backpressure, and failure isolation.

Required evidence: versioned event contract, idempotency key, partition-key
rationale, retry/DLT policy, duplicate-delivery test, and consumer-lag metric.

### gRPC

Retrieval and local inference form a typed, latency-sensitive internal boundary
that may scale independently from the public API.

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
- Record repository URL, path, commit SHA, canonical URL, product variant,
  CC BY 4.0 license metadata, ingestion time, content hash, and snapshot
  identity.
- Do not crawl `docs.github.com` or commit the acquired corpus or derived
  embeddings.
- Record model name, version/revision, checksum, license, quantization, tokenizer,
  and runtime configuration for every benchmark.
- Use synthetic or explicitly licensed fixtures in CI.

## Reliability and Security

- Treat retrieved text as untrusted context that cannot override system
  instructions.
- Treat submitted workflow fragments and logs as untrusted and potentially
  secret-bearing. Do not persist or log their raw content by default.
- Bound source-file size, include-expansion depth, parsing concurrency, and
  supported content types.
- Propagate correlation IDs and deadlines across REST, Kafka, and gRPC.
- Define health/readiness separately from dependency availability.
- Make ingestion safe under retries and duplicate Kafka delivery.
- Never describe the system as production-ready, scalable, accurate, or secure
  without bounded evidence.

## Testing and Evaluation

- Unit tests for source rendering, request-context extraction, chunking, fusion,
  thresholds, and citation assembly.
- Testcontainers integration tests for PostgreSQL/pgvector, Flyway, and Kafka.
- Contract compatibility tests for protobuf and public API schemas.
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
