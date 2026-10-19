# Development Guide

## Scope and delivery discipline

- Work only within the active milestone identified in `CURRENT_FOCUS.md`.
  Future milestones and target architecture are context, not authorization to
  design or implement them.
- Before proposing or changing code, state the active milestone, the user or
  operator outcome, the acceptance evidence, and the explicit non-goals.
- Let the product goal determine what is built. Require evidence for delivered
  behavior; do not add capabilities outside the accepted product scope.
- Prefer the smallest complete vertical slice that proves the agreed behavior.
  Do not introduce abstractions, service boundaries, configuration, extension
  points, or refactors solely for hypothetical future use.
- A technology named in the target stack is not, by itself, authorization to
  introduce it. Add it only when it serves the active milestone and its
  behavior can be tested.
- **Architecture decision (2026-10-19):** M10 plans Kafka-backed Sync through
  api-service and ingestion-service; M11 provisionally plans existing retrieval
  through gRPC. Minimize supporting machinery and preserve the accepted scope.
  Planned boundaries do not establish production scale or performance benefits.
- Starting with the first product vertical slice, backend, frontend, tests, and
  acceptance evidence belong to the same milestone unless Aleksei explicitly
  changes the boundary. Backend or infrastructure alone does not complete a
  vertical slice.
- Decompose only the active milestone into implementation tasks. The next
  milestone may have a provisional outline; later milestones keep only their
  outcome, boundary, frontend surface, and acceptance condition until they
  become active.
- If discovered work is not required by current acceptance, is not a blocker,
  and does not prevent a material security or data-loss risk, record it in
  `docs/FUTURE_CAPABILITIES.md` instead of implementing it.
- Keep implemented, in-progress, target, and future capabilities explicitly
  separated in code reviews and documentation. Never present a target boundary
  or selected technology as an existing capability.
- Use plain, concrete names for milestones and tasks. Define project-specific
  terms whose meaning is not obvious from the user scenario.
- Stop at the agreed acceptance boundary. Do not add adjacent cleanup or
  improvements without explicit approval.
- Use `.github/ISSUE_TEMPLATE/scoped-task.md` to define implementation tasks
  and `.github/pull_request_template.md` to report scope and evidence. Keep the
  templates concise; do not create per-commit governance documents.

## Milestone, Task, and Commit Decomposition

- Every new milestone starts with task 0 (`M6T0`, `M7T0`, and so on). Task 0
  is the explicit planning and scope-freezing task: it defines the milestone's
  concrete outcome and acceptance evidence, decomposes it into delivery tasks,
  and records the boundaries of each task. It contains no product
  implementation. Begin task 1 only after task 0 is accepted.
- `CURRENT_FOCUS.md` is a chronological local development record as well as the
  active tracker. When a new milestone begins, move the completed milestone's
  full record below the active milestone rather than deleting or reducing it to
  a status line. Preserve its outcome, task boundaries, material decisions, and
  acceptance evidence so retrospective review can reconstruct the development
  path. Keep only the active milestone and its next milestone outline in the
  immediately visible working area.
- Every milestone ends with a named finalization task. It contains only fixes,
  small usability improvements, and acceptance-evidence corrections discovered
  while delivering that milestone. It re-runs the milestone acceptance story
  after those corrections. Do not use it for new capabilities, changed product
  scope, or unrelated refactoring.
- Define a milestone by one verifiable user or operator outcome. Decompose it
  into separate tasks only when a task delivers independently useful behavior,
  resolves material uncertainty or risk, can be accepted independently, has a
  genuinely different owner or external dependency, or requires an isolated
  migration, security boundary, rollout, or failure proof.
- Treat technical layers and sequential construction work such as an API
  client, UI states, acceptance checks, and documentation updates as
  implementation steps inside the same delivery task when they jointly prove
  one outcome. Do not promote them to roadmap tasks merely to make the plan
  appear atomic.
- Treat milestone opening, milestone closing, boundary restatement, status
  updates, evidence recording, and required documentation as checklist or
  definition-of-done items within the task they support unless they satisfy the
  separate-task criteria above.
- Use small, logically complete commits to preserve reviewability and TDD
  progress without creating a separate task for every commit. Task boundaries
  express independently acceptable outcomes; commit boundaries express safe,
  understandable implementation increments.
- Optimize MVP delivery for the fewest coordination boundaries that still
  preserve required behavior, failure handling, tests, and acceptance evidence.
  When several proposed tasks must all finish before any user or operator value
  exists, consolidate them into one complete vertical-slice task.

## Plain-Language Outcomes and Proof

- State every milestone outcome and task outcome as a concrete scenario:
  **who does what, and what they can observe when it works**. Use product
  words such as operator, Source, Sync, document, and result; define any term
  that is not obvious from that scenario.
- For every task and milestone, include a short **What proves it is done**
  section. Describe concrete starting conditions, actions, and observable
  results. For example: "a repeated sync creates no duplicate documents," not
  "current-state semantics are preserved."
- Tests, commands, screenshots, and traces are evidence for a scenario; name
  the scenario they prove. Do not present a testing mechanism by itself as the
  task outcome or acceptance condition.
- Keep technical constraints and exclusions under **Boundaries** or
  **Non-goals**, not under proof. For example, "do not add Kafka" is a scope
  limit, not evidence that a synchronization task is complete.
- Split a milestone only when each task can have its own plain-language outcome
  and its own independent proof. Otherwise, keep the steps as checkpoints
  within one task.

## Immediate Scope Gate

- Before proposing the next code or test change, state only:
  1. the current active task's concrete user scenario;
  2. the one missing observable behavior in that scenario;
  3. the smallest change that proves or implements that behavior.
- Do not introduce a new entity, API shape, database table, configuration field,
  frontend control, architectural pattern, or acceptance scenario unless it is
  required by that missing behavior or Aleksei explicitly asks for it.
- Treat architecture questions as questions, not authorization to redesign the
  model. When Aleksei corrects scope, return to the last agreed user scenario
  and give only the next concrete step. Do not replan the milestone, expand its
  acceptance criteria, or propose future architecture unless asked.

## Collaboration

- Aleksei implements backend changes unless he explicitly delegates a particular
  implementation. Inspect, explain, review, and provide precise guidance without
  modifying backend files on his behalf by default.
- Explain material design choices, viable alternatives, failure modes, and
  verification evidence concisely. Do not introduce unrelated mechanisms.
- Read additional local instructions in `private/AGENTS.md` when present.
  Keep personal collaboration context in private files, not public documents.

## Git Restriction

Do not execute Git commands in this dialogue. This prohibition includes both
mutating and read-only commands, including `git status`, `git diff`, `git log`,
`git show`, `git add`, `git commit`, `git switch`, `git checkout`, `git merge`,
`git push`, and `git pull`. When Git information or an operation is needed,
provide the exact command for Aleksei to run and review only the output he
shares in chat.

## Task Branch and Commit Naming

- Name feature task branches as
  `feature/m<milestone-number>t<task-number>-<task-description>`. For example,
  M3.1 uses
  `feature/m3t1-publisher-api-contract-and-local-browser-access`.
- Begin each task commit message with its lowercase milestone and task number,
  followed by the conventional commit description. For example:
  `m3t1 feat(frontend): document publisher runtime boundary`.

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

Read `CURRENT_FOCUS.md`, `PRODUCT_SPEC.md`, and `TECH_STACK_SPEC.md` before
changing application behavior. Keep them current when an architectural, scope,
or active-milestone decision changes.

## Repository Layout

- `backend/` — Java 25 and Spring Boot services.
- `frontend/` — React and TypeScript web client.
- `infra/` — Docker Compose, database migrations, and local infrastructure configuration.
- `docs/` — architecture decision records and engineering notes.

## Service Boundaries and Delivery Status

- `api-service` is the target public REST boundary for the React client and
  chat orchestration; currently it proves only its implemented endpoints.
- `ingestion-service` owns the implemented synchronous ingestion behavior.
  M10 plans Kafka-backed delivery through api-service; this is not implemented
  until the active task proves the complete operator flow.
- `retrieval-service` is the provisional M11 extraction target using gRPC.
  Retrieval is still in-process; ownership and contracts wait for M11T0.

These boundaries are not an active implementation plan or evidence that every
service exists. Introduce a physical boundary only when `CURRENT_FOCUS.md`
authorizes it and an ADR compares it with an in-process module. Ship its working
contract, failure semantics, and tests in the same milestone.

## RAG Rules

These are release invariants for the stages where they apply. They do not
authorize retrieval, reranking, generation, or ingestion work before the
corresponding milestone becomes active.

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
- Evaluate `bge-reranker-v2-m3` through ONNX Runtime against the hybrid
  baseline and retain reranking before generation only if it passes the agreed
  quality gate.
- Generate with `qwen3:8b` through Ollama and return source citations with every answer.
- Return a proposed root cause, explicit assumptions, and corrective guidance
  only when retrieved evidence supports them.
- Request missing context or abstain when the evidence does not support a
  diagnosis.
- Treat retrieved content as untrusted data that cannot override system instructions.
- Treat submitted workflows and logs as potentially secret-bearing; do not log
  or persist their raw content by default.

## Target Technology Choices

These choices describe the intended v1 implementation where the active
milestone demonstrates their need. They do not override the scope and delivery
rules above or authorize work outside `CURRENT_FOCUS.md`.

- Java 25, Spring Boot, and Spring AI 2.0.0 for backend services.
- PostgreSQL with pgvector for persistence and retrieval.
- Apache Kafka is selected for M10 asynchronous Source synchronization.
- gRPC is selected for provisional M11 internal retrieval; implementation
  waits for M11T0 and its ADR.
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
