# Documentation Integrity

A production-shaped Java RAG system for diagnosing GitHub Actions problems with
measurable, cited answers over versioned product documentation.

The initial demonstration serves developers and DevOps engineers responsible
for CI/CD workflows. It uses the GitHub Actions documentation from the official
`github/docs` repository as its licensed knowledge corpus. The system is an
independent technical project and is not affiliated with, endorsed by, or
sponsored by GitHub.

## Problem

Diagnosing a GitHub Actions failure can require connecting workflow syntax,
secret propagation, token permissions, trigger behavior, and runner context
across several documentation pages. An AI chat interface can still conceal a
retrieval failure behind a fluent explanation. This project makes the proposed
root cause, supporting evidence, abstention, retrieval quality, and later corpus
freshness observable and testable.

Chat is the first demonstration surface. The product direction is a knowledge
quality platform for changing documentation.

## Current Status

This repository is in the executable-foundation phase. The table distinguishes
working capabilities from target architecture.

| Capability | Status |
| --- | --- |
| Docker Compose with PostgreSQL/pgvector and Kafka | Implemented and verified locally |
| Java 25 Maven reactor and `api-service` health endpoint | In progress |
| Versioned schema migrations and integration tests | Planned next |
| Allowlisted ingestion with provenance and idempotency | Planned |
| Hybrid retrieval, reranking, citations, and abstention | Planned |
| Evaluation suite and observability | Planned |
| React chat UI | Planned |
| Change, freshness, and contradiction detection | Later milestone |

## Target Architecture

- Java 25, Spring Boot 4.x, Spring AI 2.0.0, and Maven
- React and TypeScript frontend
- PostgreSQL with pgvector, HNSW vector search, and GIN full-text search
- Apache Kafka for durable asynchronous ingestion
- gRPC for the typed internal retrieval boundary
- Ollama with `qwen3:8b` for answer generation
- ONNX Runtime with `nomic-embed-text` and `bge-reranker-v2-m3`
- Micrometer/OpenTelemetry instrumentation and a versioned evaluation dataset

Kafka and gRPC will be introduced only with their working boundaries, failure
semantics, and tests. Their presence in the target architecture is not a claim
that they are implemented today.

## Initial Demonstration

A developer can submit a natural-language question together with a workflow
fragment or error message. The system returns an evidence-backed diagnosis,
relevant assumptions, a minimal corrective example when supported, and direct
source citations. If the provided context or corpus cannot support a diagnosis,
the system asks for the missing context or abstains.

Initial question categories include reusable workflows, secrets and
permissions, workflow triggers, `GITHUB_TOKEN`, runners, and deployment
environments.

## Knowledge Source Policy

Ingestion is limited to explicitly allowlisted sources whose automated access,
retention, and use are permitted. Public availability alone is not treated as
permission to crawl or store content. No third-party documentation corpus or
downloaded model is committed to this repository.

The initial corpus is the current GitHub.com variant of `content/actions/` from
the official [`github/docs`](https://github.com/github/docs) repository,
including referenced content from `data/reusables/`. That documentation is
licensed under CC BY 4.0. Referenced values from `data/variables/` are resolved
as part of rendering. The corpus is acquired from a pinned Git revision and
parsed locally; this project does not crawl `docs.github.com`.

Every indexed document retains its repository path, source commit, canonical
URL, content hash, product variant, and license metadata. Synthetic fixtures are
used for automated tests and public CI.

## Local Infrastructure

Prerequisite: Docker Desktop.

```bash
docker compose -f infra/compose.yaml up -d
docker compose -f infra/compose.yaml ps
```

PostgreSQL is available on `localhost:5444`; Kafka is available on
`localhost:29092`.

## Planned Proof

The project will publish reproducible comparisons of full-text search, vector
search, hybrid retrieval, and hybrid retrieval with reranking. Results will
include retrieval quality, citation support, abstention behavior, latency, and
the exact dataset/model/configuration used.

## License

This project is licensed under the Apache License 2.0. See [LICENSE](LICENSE).
