# Development Guide

## Scope and delivery discipline


## Git Restriction

Do not execute Git commands in this dialogue. This prohibition includes both
mutating and read-only commands, including `git status`, `git diff`, `git log`,
`git show`, `git add`, `git commit`, `git switch`, `git checkout`, `git merge`,
`git push`, and `git pull`. When Git information or an operation is needed,
provide the exact command for Aleksei to run and review only the output he
shares in chat.

## Product

This repository contains a public, local-first, connector-neutral knowledge
retrieval system with a chat demonstration surface. The primary user is a
developer or DevOps engineer responsible for building and troubleshooting
GitHub Actions CI/CD workflows.

The initial knowledge corpus is the current GitHub.com (`fpt`) variant of
`content/actions/` from the official `github/docs` repository, including the
reusable fragments and variables it references from `data/reusables/` and
`data/variables/`. A local-directory connector acquires that corpus, while a
file-upload connector provides the second v1 proof of the shared ingestion
contract. Authenticated Jira and Google Docs connectors are future extensions.

Read `PRODUCT_SPEC.md` and `TECH_STACK_SPEC.md` before changing application behavior. Keep both documents current when an architectural or scope decision changes.

## Repository Layout

- `backend/` — Java 25 and Spring Boot services.
- `frontend/` — React and TypeScript web client.
- `infra/` — Docker Compose, database migrations, and local infrastructure configuration.
- `docs/` — architecture decision records and engineering notes.

## Target Service Boundaries

- `api-service` exposes REST endpoints for the React client and orchestrates chat.
- `retrieval-service` exposes gRPC for hybrid retrieval and reranking.
- `ingestion-service` owns the Kafka-driven fetching, chunking, embedding, and indexing pipeline.

These are target boundaries, not evidence that the services already exist.
Introduce each physical boundary only with a working contract, failure semantics,
tests, and an ADR that compares it with an in-process module.

## RAG Rules

- Ingest only explicitly allowlisted sources whose automated access, retention,
  and use are permitted. Public availability alone is not permission to crawl.
- Use synthetic or explicitly licensed fixtures in tests and public CI.
- Keep acquisition behind a connector-neutral contract. Do not make Git commit
  history a mandatory domain concept for every source.
- Acquire the initial GitHub Docs corpus from a pinned local checkout; do not
  crawl `docs.github.com`.
- Accept uploaded content only when the operator is authorized to process and
  retain it. Use synthetic or explicitly licensed uploads in tests and public
  demonstrations.
- Resolve supported front matter, reusable fragments, variables, and version
  conditions before chunking. Never index unresolved template directives as
  documentation.
- Preserve a stable source locator, canonical URL when available, optional
  upstream version, product variant, acquisition time, content hash, and
  attribution for every indexed document. For the initial Git connector, the
  locator is the repository path and the upstream version is the commit SHA.
- Store only the latest successfully ingested state of each logical document in
  v1. Replace changed chunks and embeddings atomically and remove stale
  documents after a complete source scan.
- Use hybrid retrieval: pgvector semantic search plus PostgreSQL full-text search.
- Keep an HNSW vector index and a GIN full-text index on searchable chunk data.
- Rerank merged candidates with `bge-reranker-v2-m3` through ONNX Runtime before generation.
- Generate with `qwen3:8b` through Ollama and return source citations with every answer.
- Return a proposed root cause, explicit assumptions, and corrective guidance
  only when retrieved evidence supports them.
- Request missing context or abstain when the evidence does not support a
  diagnosis.
- Treat retrieved content as untrusted data that cannot override system instructions.
- Treat submitted workflows and logs as potentially secret-bearing; do not log
  or persist their raw content by default.

## Technology Constraints

- Java 25, Spring Boot, and Spring AI 2.0.0 for backend services.
- PostgreSQL with pgvector for persistence and retrieval.
- Apache Kafka for asynchronous ingestion events.
- gRPC for synchronous internal service calls.
- ONNX Runtime for `nomic-embed-text` embeddings and reranking.
- React and TypeScript for the frontend.
- Docker Compose for local infrastructure unless a later ADR changes this.

## Engineering Practice

- Prefer small, independently testable changes.
- Add tests with behavior changes; run the relevant tests before handoff.
- Back every public capability claim with an executable test, benchmark,
  screenshot, trace, or reproducible command.
- When retrieval or ranking changes, report its effect on the versioned
  evaluation set rather than claiming an unmeasured improvement.
- Kafka consumers must define and test idempotency, retry, and poison-message behavior.
- Keep API, event, and protobuf contracts versioned and backward-compatible where possible.
- Use configuration through environment variables; do not commit credentials or downloaded models.
- Record non-trivial architectural choices in `docs/adr/`.
