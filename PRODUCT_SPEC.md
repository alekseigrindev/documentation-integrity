# Product Specification — v1.0.0

## Product Name

Documentation Integrity

## Product Thesis

GitHub Actions failures often require a developer to connect workflow syntax,
secret propagation, token permissions, trigger behavior, and runtime context
across several documentation pages. A fluent AI answer can still hide an
incorrect retrieval result or an unsupported diagnosis. The system makes the
proposed root cause, evidence, retrieval quality, citations, and abstention
observable and testable.

Chat is the required v1 interface. The longer-term product is a knowledge
quality platform with freshness, change, and contradiction analysis.

The ingestion core is connector-neutral. Git is the acquisition mechanism for
the initial licensed corpus, not a mandatory versioning model for every future
source.

## Target Users

- Primary: a developer or DevOps engineer responsible for building and
  troubleshooting GitHub Actions CI/CD workflows.
- Operator: an engineer or knowledge owner who ingests approved documentation,
  maintains evaluation cases, and investigates quality regressions.

The primary job is: given a workflow fragment, observed behavior, or error
description, identify the likely cause and produce an evidence-backed
explanation or explicitly abstain.

## User Problems

- Troubleshooting frequently requires combining facts from workflow syntax,
  security, reusable-workflow, and trigger documentation.
- Search returns relevant pages but does not diagnose how their constraints
  interact in the submitted workflow.
- Generic chat can conceal retrieval failures behind fluent output.
- Users need the proposed root cause, assumptions, corrective guidance, and
  citations to be distinguishable from model speculation.
- Users need an explicit request for missing context or a refusal when the
  corpus does not support a diagnosis.
- Operators need repeatable evidence that retrieval changes improve quality.

## Core Scenarios

1. A developer submits a reusable-workflow fragment whose deployment secret is
   unavailable in a nested workflow. The system identifies direct-call secret
   propagation as the supported root cause and cites the relevant documentation.
2. A developer asks why a pull request created with `GITHUB_TOKEN` did not
   trigger another workflow. The system explains the recursion-prevention
   behavior and cites supported authentication alternatives.
3. An ambiguous, unsupported, or insufficiently specified problem causes the
   system to request the missing context or explicitly abstain.
4. An operator runs the versioned evaluation suite and compares retrieval and
   reranking configurations.
5. An operator imports an authorized Markdown or text file through the same
   normalization, provenance, indexing, and retrieval pipeline used for the
   initial local documentation checkout.

## v1.0.0 Scope

- One conversational interface accepting a natural-language problem and an
  optional workflow fragment or error excerpt.
- Manual ingestion triggered by an admin API or UI action.
- A connector-neutral acquisition contract with two adapters: a local
  directory and an operator-provided Markdown or text file.
- A pinned local checkout of the official `github/docs` repository as the
  initial licensed corpus.
- The current GitHub.com (`fpt`) variant of `content/actions/` as the initial
  corpus, including referenced fragments and values from `data/reusables/` and
  `data/variables/`.
- Local parsing of Markdown, front matter, reusable fragments, variables, and
  supported version conditions; no HTTP crawl of `docs.github.com`.
- Provenance for every indexed chunk: source, stable source locator, canonical
  URL when available, optional upstream version, product variant, attribution,
  ingestion time, and content hash. For the initial Git corpus, the upstream
  version is the commit SHA.
- Current-state storage for logical documents, identified by source, product
  variant, and stable source locator.
- Idempotent ingestion of unchanged documents and transactional replacement of
  changed documents with their chunks and embeddings.
- An observable source ingestion attempt recording status, timing, and
  failures without pretending that every source has a global revision. A
  failed attempt may leave partially synchronized current state; the operator
  searches only after a successful attempt.
- PostgreSQL full-text and pgvector retrieval with a documented fusion method.
- Local cross-encoder reranking before answer generation.
- Retrieval-grounded diagnosis with assumptions, source citations, and a
  minimal corrective example only when supported by evidence.
- A request for missing context or explicit abstention when retrieved evidence
  is insufficient.
- A versioned offline evaluation dataset and reproducible comparison report.

## Explicitly Out of Scope for v1.0.0

- Authenticated access to GitHub organizations, repositories, Actions runs, or
  logs.
- Authenticated Jira and Google Docs connectors. They are planned extension
  points after the connector contract is proven without OAuth and proprietary
  test data.
- Executing, editing, or committing a user's workflow.
- Receiving or storing real credentials, tokens, or secrets.
- GitHub Docs product variants other than the current GitHub.com (`fpt`)
  documentation.
- GitHub documentation outside the selected Actions corpus.
- Internet-wide or unauthorized crawling.
- Distribution of third-party documentation content in the repository.
- Scheduled and continuous re-ingestion.
- Automated freshness and contradiction detection; these belong to the next
  product milestone.
- Any claim of official GitHub affiliation, endorsement, or support.

## Initial Acceptance Targets

The following are targets, not current performance claims:

- A versioned evaluation set with at least 30 reviewed cases covering workflow
  syntax, secrets and permissions, reusable workflows, triggers and
  `GITHUB_TOKEN`, runners and environments, ambiguous questions, unanswerable
  questions, and adversarial content.
- At least 80% of answerable cases identify the reviewed primary cause under the
  documented evaluation rubric.
- At least 85% Recall@10 for passages marked relevant in the evaluation set.
- Every non-abstaining answer includes at least one source citation.
- At least 90% of cited passages support the associated answer claim under the
  documented review rubric.
- The reranked configuration is compared against lexical-only, vector-only, and
  hybrid-without-reranking baselines using MRR or nDCG.
- Re-ingesting unchanged documents creates no duplicate logical chunks.
- Re-ingesting a changed document leaves only its new chunks searchable.
- The local-directory and file-upload adapters produce the same normalized
  document contract and provenance guarantees.
- End-to-end and stage-level p50/p95 latency are reported on documented
  hardware; a latency target is set after the first baseline.

## Next Product Milestone

- Add authenticated Jira and Google Docs connectors with explicit permission,
  secret-handling, pagination, deletion, and rate-limit semantics.
- Preserve and compare corpus snapshots when concurrent ingestion and search or
  atomic corpus rollback becomes a demonstrated requirement.
- Mark answers affected by source changes.
- Identify candidate contradictions with supporting passages.
- Run regression evaluation when the corpus, model, prompt, or ranking
  configuration changes.
- Expose quality, freshness, latency, and ingestion-health dashboards.

## Open Questions

1. Should ingestion use a constrained project-owned renderer or invoke the
   upstream GitHub Docs rendering toolchain for reusables, variables, and
   version conditions?
2. What deployment target can support the public demo without misrepresenting
   the local-first model architecture?
3. What model quantization and concurrency fit the documented hardware budget?
4. Which latency and abstention thresholds should be frozen after the baseline?
5. What automated redaction can reduce the risk of users submitting credentials
   inside workflow fragments or logs?
