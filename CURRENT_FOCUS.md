# Current Focus

This file is the local working tracker for the active milestone and a
chronological record of completed milestones. When a new milestone begins, move
the completed milestone's full record below the active working area rather than
deleting or condensing it, so development decisions and evidence remain
available for retrospective review.

## Previously Completed Milestones

- **M3 — Publisher management vertical slice — Completed 2026-09-02**
- **M4 — Source management vertical slice — Completed 2026-09-02**
- **M5 — Source synchronization vertical slice — Completed 2026-09-08**
- **M6 — Citable lexical search and evaluation preparation — Completed 2026-09-10**
- **M7 — Source-scoped lexical search and evaluation — Completed 2026-09-16**
- **M8 — Measured vector and hybrid retrieval — Completed 2026-09-23**

## Active Milestone

**M9 — Measured reranking**

**Plain outcome:** A user can run a reranked retrieval configuration and still
receive cited passages. An operator compares it with the lexical, vector, and
hybrid baselines using passage-level relevance judgments and retains the
reranker only when the measured quality gain justifies its latency and resource
cost.

## M9 Task Tracker

| Task | Plain outcome | Status |
| --- | --- | --- |
| M9.0 Milestone definition | The team defines passage-level relevance evidence, reranking metrics, candidate retrieval, runtime limits, and delivery boundaries before implementation. | Completed 2026-09-23 |
| M9.1 Passage-level retrieval evaluation | The operator evaluates exact reviewed passages rather than treating every chunk from the expected document as relevant. | Completed 2026-09-24 |
| M9.2 Reranker integration | A user selects reranked retrieval and receives reordered cited passages through the existing search flow. | In progress |
| M9.3 Measured reranker selection | The operator compares the working reranker with the strongest baseline and accepts or rejects it using the frozen gate. | Planned |
| M9.4 Milestone finalization | The M9 evaluation and reranking story works after small corrections discovered during delivery. | Planned |

## Active Task

**M9T2 — Reranker integration**

### M9T0 — Milestone definition

**Proposed branch:** `feature/m9t0-milestone-definition`

**What proves it is done:** The tracker defines stable passage-level relevance
judgments that do not depend on regenerated database UUIDs, correct names and
formulas for the evaluation metrics, the reranker input and output contract,
the baseline configurations, the latency and resource budget, delivery tasks,
and explicit non-goals. M9T0 contains no product implementation or model
download.

**Planning constraint:** The M7/M8 evaluator treats any returned chunk from an
expected document as a hit. Its reported `Recall@10` is therefore a
document-level HitRate@10, not passage-level recall, and it does not calculate
precision. M9 must correct this methodology before using it to accept or reject
the reranker.

### M9T1 — Passage-level retrieval evaluation

**Proposed branch:** `feature/m9t1-passage-level-retrieval-evaluation`

**User and operator scenario:** An operator records the exact passages expected
to answer each question in the existing 12-case evaluation set. The evaluator
matches returned passages by locator and content hash rather than accepting any
chunk from the expected document.

**What proves it is done:** Evaluation-set version 2 contains the existing 12
reviewed cases and 14 expected passages. Every expected passage is identified
by `sourceLocator` and the backend-produced `chunkContentHash`. The evaluation
records and report calculate passage-level HitRate@10, Precision@10, reviewed
Recall@10, MRR@10, and p50/p95 latency. The backend compiles, the JSON structure
is valid, and all 14 locator/hash pairs resolve to current stored chunks.

**Evaluation rules:** The pinned corpus revision and evaluation-set version are
recorded with the expected passages. Each query maps to one or more manually
reviewed expected chunks; a returned chunk is relevant only when its locator
and content hash match that set. `Recall@10` is explicitly limited to this
reviewed expected-passage set. Runtime database UUIDs are not stored as
evaluation identities. When corpus rendering or chunking changes, affected
hashes are reviewed and the evaluation-set version is incremented. Graded
relevance is deferred until a demonstrated evaluation need requires it.

**Boundaries:** Generalize the existing evaluation records, service, API,
downloaded report, and frontend display only as required by the corrected
metrics. Expanding the set to 30 cases, adding nDCG@10 and the controlled
comparative reranker report continue only after the reranker exists. Do not
persist reports in the database, add evaluation history, or create a separate
evaluation service.

### M9T2 — Reranker integration

**Proposed branch:** `feature/m9t2-reranker-integration`

**User and operator scenario:** A user chooses reranked retrieval and receives
the ten highest-ranked cited passages for all Sources or selected Sources. If
the model is unavailable, the method is not offered; a request that fails
during reranking returns an explicit failure instead of silently presenting
baseline results as reranked results.

**Retrieval flow:** Lexical and vector retrieval each produce up to 50
candidates. Reciprocal Rank Fusion deduplicates and orders the combined pool
without applying the final ten-result cutoff. `bge-reranker-v2-m3` scores at
most the first 50 fused candidates for the query through ONNX Runtime. The
application sorts those scores and returns ten existing cited passages; the
reranker cannot invent content or change citation provenance.

**What proves it is done:** A controlled case places a reviewed relevant
passage inside the 50-candidate pool and proves that reranking can move it into
the returned top 10 without changing its citation. Source filtering still
works. The method is exposed to Search and Evaluation only while the reranker
is available. Model loading and one bounded request complete without exhausting
the documented Apple M3 Max 36 GB host. This task proves a working retrieval
path but does not claim that reranking improves quality or should become the
default.

**Boundaries:** Use `bge-reranker-v2-m3` through ONNX Runtime inside the
existing application boundary with bounded batch and candidate counts. Record
the exact model revision, checksum, license, tokenizer, warm-up procedure, and
runtime settings. Do not add generation, chat, abstention, Kafka, gRPC, a
physical retrieval-service split, GPU support, or evaluation-report storage.
Do not tune or accept the reranker from an ad hoc browser example.

### M9T3 — Measured reranker selection

**Proposed branch:** `feature/m9t3-measured-reranker-selection`

**User and operator scenario:** An operator runs the same version-2
passage-level evaluation for the strongest non-reranked baseline and the
working reranked method. The report shows candidate coverage, passage quality,
latency, and memory, and the operator records whether the reranker is retained
or rejected.

**What proves it is done:** Candidate HitRate@50 is reported so the reranker is
not blamed for relevant passages missing from its input. The two configurations
run against the same pinned corpus, reviewed judgments, candidate limit, result
limit, model configuration, and documented host. The report and milestone
record contain the measurements and an explicit accept-or-reject decision.

**Acceptance gate:** Relative to the strongest non-reranked baseline,
reranking must improve nDCG@10, must not reduce HitRate@10 or reviewed
Recall@10, and must keep warmed end-to-end p95 below 3 seconds on the documented
Apple M3 Max 36 GB host. MRR@10, Precision@10, p50/p95, peak process memory,
candidate count, and reranker batch size are also reported. Failure, model
unavailability, or excessive resource use rejects the reranked method rather
than silently falling back under the same method name.

**Boundaries:** Use the implementation completed in M9T2 and the evaluation
methodology completed in M9T1. Do not change judgments, candidate depth,
quality thresholds, or latency budget after seeing the final comparison. Do
not add another model, tune against individual evaluation cases, or implement
generation, chat, or report persistence.

### M9T4 — Milestone finalization

**Proposed branch:** `feature/m9t4-reranking-finalization`

**Purpose:** Implement only small fixes, usability improvements, and
acceptance-evidence corrections explicitly discovered during M9. Re-run the
passage-level baseline and reranked comparison after those corrections. Do not
add a new retrieval capability or relax the frozen quality and runtime gate.

### M9 Non-goals

- No answer generation, diagnosis, chat, or abstention behavior.
- No evaluation history or report database.
- No Kafka, gRPC, physical retrieval-service split, or GPU execution.
- No use of generated chunk UUIDs as durable evaluation identities.
- No claim that reranking improves retrieval unless the version-2 evaluation
  passes the frozen quality and runtime gate.

## Completed Milestone Record

**M8 — Measured vector and hybrid retrieval — Completed 2026-09-23**

**Plain outcome:** A user can search synchronized documentation with lexical,
vector, or hybrid retrieval and still receive cited passages. An operator runs
the same versioned evaluation against each method, sees quality and latency,
and keeps the best method only when the measurements justify it.

## M8 Task Tracker

| Task | Plain outcome | Status |
| --- | --- | --- |
| M8.0 Milestone definition | The team agrees the M8 retrieval methods, quality gate, delivery tasks, runtime constraints, and boundaries before implementation begins. | Completed |
| M8.1 Embedding-backed vector retrieval | Synchronization creates current embeddings, and a user can run cited vector search over synchronized Sources. | Completed 2026-09-22 |
| M8.2 Measured hybrid retrieval selection | A user can choose lexical, vector, or hybrid search, and an operator compares them on the versioned evaluation set. | Completed 2026-09-23 |
| M8.3 Milestone finalization | The M8 retrieval story works after small fixes and usability improvements discovered during delivery. | Completed 2026-09-23 |

## M8 Delivery Tasks

**M8T2 — Measured hybrid retrieval selection**

### M8T0 — Milestone definition

**Proposed branch:** `feature/m8t0-milestone-definition`

**What proves it is done:** This tracker and the development milestone plan
agree on one measured vector-and-hybrid retrieval outcome, its quality gate,
delivery tasks, runtime evidence, and exclusions. M8T0 contains no product
implementation.

### M8T1 — Embedding-backed vector retrieval

**Proposed branch:** `feature/m8t1-embedding-backed-vector-retrieval`

**User and operator scenario:** An operator synchronizes a Source and the
system creates an embedding for every current searchable chunk. A user chooses
vector search, enters a natural-language query, and receives cited passages
from all synchronized Sources or the selected Sources.

**What proves it is done:** Synchronizing a controlled Source stores one
current vector per searchable chunk. A known semantic query through the browser
returns its expected cited passage using vector search. Re-synchronizing
unchanged content creates no duplicate vectors, and replacing content leaves
only vectors for current chunks searchable.

**Boundaries:** Use `nomic-embed-text` through ONNX Runtime in the existing
application boundary and store vectors in PostgreSQL with pgvector. Keep the
existing Source filter and citation contract. Do not add hybrid fusion,
reranking, chat, gRPC, a physical retrieval service, or evaluation history.
Downloaded model artifacts and derived embeddings are not committed to Git.

**M8T1 evidence:** A local synchronization stored embeddings for all 11,069
current chunks; an unchanged repeat left the same counts. Vector search in the
browser returned cited passages with a selected Source. A controlled document
replacement removed the old searchable chunk and exposed the new one. On the
same unchanged corpus and 12-case evaluation set, vector found the expected
document in 12/12 cases (Recall@10 1.00, MRR@10 0.581) versus lexical 3/12
(Recall@10 0.25, MRR@10 0.139). This is preliminary evidence, not the M8
30-case quality gate or a decision to change the default.

### M8T2 — Measured hybrid retrieval selection

**Proposed branch:** `feature/m8t2-measured-hybrid-retrieval-selection`

**User and operator scenario:** A user chooses lexical, vector, or hybrid
search and receives cited passages through the same browser flow. An operator
runs the versioned evaluation for each method and compares Recall@10, MRR@10,
p50, and p95 before deciding which method should be the default.

**What proves it is done:** The evaluation set has at least 30 reviewed cases
covering the release retrieval topics. One controlled Source produces lexical,
vector, and hybrid reports from the same cases and corpus state. The selected
default does not reduce Recall@10 relative to lexical search and improves
MRR@10; if no candidate passes, lexical remains the default and the measured
rejection is recorded.

**Boundaries:** Fuse lexical and vector rankings with a documented Reciprocal
Rank Fusion configuration. Retrieval methods remain independently selectable
and evaluable. Do not add a cross-encoder reranker, answer generation, chat,
evaluation-report persistence, gRPC, or a separate deployed service.

**M8T2 evidence:** The browser exposed lexical, vector, and hybrid retrieval in
both Search and Evaluation. On the same unchanged 12-case corpus, lexical found
3/12 expected documents (reported Recall@10 0.25, MRR@10 0.139, p50 2 ms,
p95 29 ms), vector found 12/12 (1.00, 0.581, 18 ms, 36 ms), and hybrid RRF
found 11/12 (0.917, 0.568, 22 ms, 99 ms). Hybrid lost the
`environment-secrets` case that vector returned at rank 7 because RRF promoted
candidates supported by both methods. The reports are preliminary
document-level evidence; no default was changed from them.

### M8T3 — Milestone finalization

**Proposed branch:** `feature/m8t3-retrieval-finalization`

**Purpose:** Implement only small fixes, usability improvements, and
acceptance-evidence corrections explicitly discovered while delivering M8.
Re-run the selected retrieval and evaluation story after those corrections.

**Finalization decision:** Search and downloadable evaluation reports were
rechecked through the browser after hybrid retrieval was added. The planned
30-case gate was deliberately deferred when review showed that the evaluator
judges whole documents rather than relevant passages and therefore does not
measure passage precision or recall. M9T0 owns correction of that methodology
before reranker evaluation; M8 does not claim a release-quality retrieval
selection.

### Original M8 Quality Gate and Runtime Evidence

- Recall@10 and MRR@10 use the same definitions established in M7.
- A candidate replaces lexical as the default only when it does not reduce
  Recall@10 and improves MRR@10 on the same versioned cases and corpus state.
- Report p50 and p95 for lexical, vector, and hybrid retrieval on documented
  local hardware.
- Record the embedding model name, revision, checksum, license, tokenizer,
  ONNX Runtime configuration, warm-up behavior, and measured memory use.

The 30-case quality gate was not completed and is not claimed as M8 evidence.
It is replaced by a passage-level evaluation definition in M9T0 before measured
reranking begins.

### M8 Non-goals

- No reranker, diagnosis, generation, chat, or abstention behavior.
- No Kafka, gRPC, physical retrieval-service split, or evaluation database.
- No new connector, corpus-rendering expansion, or ingestion UI redesign.
- No claim that vector or hybrid retrieval is better until the evaluation
  demonstrates it.

## Completed Milestone Record

**M7 — Source-scoped lexical search and evaluation — Completed 2026-09-16**

**Plain outcome:** A user searches all synchronized documentation or selected
Sources in the browser. An operator selects the synchronized pinned GitHub
Actions Source, runs the fixed 12-case lexical evaluation, and receives one
readable report showing every case and the overall measured result.

### M7 Task Tracker

| Task | Plain outcome | Status |
| --- | --- | --- |
| M7.0 Milestone definition | The team agrees the M7 outcome, evidence, delivery tasks, measurements, and boundaries before implementation begins. | Completed 2026-09-11 |
| M7.1 Source-scoped lexical search and evaluation | A user searches all or selected Sources, and an operator runs the 12 fixed cases for one synchronized Source. | Completed 2026-09-16 |
| M7.2 Milestone finalization | The M7 evaluation story works after small fixes and usability improvements found during delivery. | Completed 2026-09-16 |

### M7 Delivery and Evidence

The browser supports full-corpus and multi-Source lexical search with cited
passages. The evaluation screen runs the fixed 12 cases for one synchronized
Source, shows per-case rank and duration plus Recall@10, MRR@10, p50, and p95,
and downloads the current report as JSON.

A real browser evaluation against the synchronized local GitHub Actions Source
produced the evaluation-set v1 lexical baseline: Recall@10 `0.25`, MRR@10
`0.13888888888888887`, p50 `2 ms`, and p95 `29 ms`. M7 finalization added
collapsible long passages and a compact, dismissible multi-Source selector.
Frontend verification passed with 43 tests, lint, and production build. Backend
automated evaluation coverage was explicitly deferred and is not claimed as
completed evidence.

**Boundaries:** M7 did not add embeddings, vector or hybrid retrieval,
reranking, chat, report persistence, or a separate evaluation service.

**M6 — Citable lexical search and evaluation preparation — Completed 2026-09-10**

**Plain outcome:** A user searches synchronized documentation and sees passages
with citations. The project also has a fixed, reviewable evaluation set and
report format for the next evaluation vertical slice.

### M6 Agreed Delivery Plan

M6 first gives the user a usable cited lexical search. It then fixes the
evaluation inputs and report format that M7 will use to run lexical retrieval
from the browser and show the operator measured results.

M6 uses a reviewed evaluation set of 12 search cases. Each case records a user
query and the source locator or locators that count as correct evidence. The
30-case release evaluation remains M8 work.

The M6 evaluation data defines the comparison rule for later retrieval methods:
a candidate replaces lexical search only when it finds the correct evidence in
the first ten results at least as often and places that evidence higher. The
later comparison also records typical and slow search latency.

## M6 Task Tracker

| Task | Plain outcome | Status |
| --- | --- | --- |
| M6.0 Milestone definition | The team agrees the M6 outcome, acceptance evidence, delivery tasks, and boundaries before implementation begins. | Completed 2026-09-08 |
| M6.1 Citable lexical search in the browser | A user enters a documentation query and sees matching passages with their source citation. | Completed 2026-09-08 |
| M6.2 Evaluation preparation | The project has a fixed 12-case evaluation set and report format tied to the pinned GitHub Actions corpus. | Completed 2026-09-09 |
| M6.3 Milestone finalization | The M6 user and operator stories work without known M6 defects or usability blockers. | Completed 2026-09-10 |

## M6 Task Details

### M6T1 — Citable lexical search in the browser

**Proposed branch:** `feature/m6t1-citable-lexical-search`

**User scenario:** A developer enters a documentation query and sees matching
passages with the source locator and attribution that identify where each
passage came from.

**What proves it is done:** A known query against synchronized documentation
returns a cited matching passage in the local browser. Empty and
request-failure states are clear. Frontend lint and production build pass.

**Boundaries:** Reuse the existing lexical search endpoint. Do not add
embeddings, vector or hybrid retrieval, reranking, evaluation, diagnosis, chat,
gRPC, or a new backend service.

### M6T2 — Measured retrieval selection

**Proposed branch:** `feature/m6t2-measured-retrieval-selection`

**Evaluation corpus:** the pinned `github/docs` checkout at commit
`062800c32b5d12ccae18d1a4a542e94069d827f8`, using `content/actions/`.

**Operator scenario:** Before browser evaluation is implemented, the project
has one fixed set of 12 GitHub Actions search cases and one reviewable report
format. Each case identifies the documentation locator that counts as correct
evidence.

**What proves it is done:** The versioned case file contains 12 reviewed cases,
each with a query and expected locator, tied to the pinned corpus commit. The
versioned report format has one result slot per case and method, plus method
summaries and selected-method fields, but contains no fabricated measurements.

**Boundaries:** Do not add an evaluation runner, endpoint, frontend evaluation
screen, database table, embeddings, vector retrieval, hybrid retrieval,
reranking, diagnosis, chat, gRPC, or a new service. Building the 30-case
release evaluation set belongs to M8.

### M6T3 — Milestone finalization

**Proposed branch:** `feature/m6t3-retrieval-finalization`

**Purpose:** Implement the small fixes, usability improvements, and acceptance
evidence corrections that were explicitly noticed while delivering M6. This is
not a review, audit, or reopening of completed M6 work.

### Recorded Corrections

1. **Successful ingestion-run change counts.** The operator sees documents
   added, updated, and removed on a successful ingestion run. Failed runs
   continue to show a safe failure result without change counts; no
   per-document audit history is added.

   **Proof:** A controlled sync that adds, updates, and removes known documents
   shows matching document counts in **Ingestion runs**.

2. **Default Source key.** When creating a Source, the form generates a usable
   Source key while the operator types its name. The operator can review or
   replace the generated value; editing an existing Source does not silently
   replace its stored key.

   **Proof:** Typing a name for a new Source fills the key field, and the
   operator can replace the generated value before saving.

3. **Search result count.** After a documentation search, the user sees the
   number of matching passages returned by that search.

   **Proof:** A completed search with matches displays the matching result
   count beside its results.

4. **Blank local Markdown files.** A local-directory synchronization skips
   Markdown files that are empty or contain only whitespace, then continues
   with the remaining readable documents. A previously indexed file that
   becomes blank is removed as missing from the completed scan.

   **Proof:** A directory containing one blank Markdown file and one readable
   Markdown file completes successfully and indexes the readable file.

**Deferred from M6:** Backend-local Source directory/file validation and
single-file scanner support are recorded in `future_fixes.md`. Browser-side
selection and upload remain a separate future capability.

**Task completion:** Every remaining recorded correction above works in its
affected story. The cited browser search still works, and the 12-case
evaluation data and report format remain valid. This is a targeted recheck, not
a review of all M6 implementation.

**Boundaries:** Include only these corrections and another small M6 correction
that is first recorded here. Do not add new retrieval capabilities, change the
agreed quality gate, add chat, add a connector type, add a file-upload workflow
or ingestion-run API, or make unrelated refactors.

### What Proves M6T0 Is Done

- `CURRENT_FOCUS.md` contains the agreed M6 plan and all M6 delivery tasks.
- Each delivery task has a concrete user or operator scenario, observable
  proof, and explicit boundaries.
- M6 uses 12 reviewed cases now; M8 owns the 30-case release evaluation.

### M6T0 Boundaries

- M6T0 contains no product code, API change, database migration, or frontend
  behavior.
- M6T1 begins only after M6T0 is accepted.
