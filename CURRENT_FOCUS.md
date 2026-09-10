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

## Active Milestone

**M6 — Citable lexical search and evaluation preparation**

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
| M6.3 Milestone finalization | The M6 user and operator stories work without known M6 defects or usability blockers. | In progress |

## Active Task

**M6T3 — Milestone finalization**

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

2. **Local Source targets and validation.** The operator can configure a local
   Source using either a readable directory or one readable Markdown file, then
   synchronize that target. `SourceService` validates the readable `file:` URL
   during registration and update. `LocalDirectorySourceScanner` retains only
   runtime access handling for a target that changes or disappears afterwards.

   **Proof:** A selected local directory and a selected local Markdown file
   both synchronize successfully. Invalid local targets are rejected during
   Source registration or update.

3. **Default Source key.** When creating a Source, the form generates a usable
   Source key while the operator types its name. The operator can review or
   replace the generated value; editing an existing Source does not silently
   replace its stored key.

   **Proof:** Typing a name for a new Source fills the key field, and the
   operator can replace the generated value before saving.

4. **Search result count.** After a documentation search, the user sees the
   number of matching passages returned by that search.

   **Proof:** A completed search with matches displays the matching result
   count beside its results.

5. **Blank local Markdown files.** A local-directory synchronization skips
   Markdown files that are empty or contain only whitespace, then continues
   with the remaining readable documents. A previously indexed file that
   becomes blank is removed as missing from the completed scan.

   **Proof:** A directory containing one blank Markdown file and one readable
   Markdown file completes successfully and indexes the readable file.

**Task completion:** Every recorded correction above works in its affected
story. The cited browser search still works, and the 12-case evaluation data
and report format remain valid. This is a targeted recheck, not a review of all
M6 implementation.

**Boundaries:** Include only these corrections and another small M6 correction
that is first recorded here. Do not add new retrieval capabilities, change the
agreed quality gate, add chat, add a connector type, add a file-upload workflow
or ingestion-run API, or make unrelated refactors.

## Next Milestone (Provisional)

**M7 — Evaluation vertical slice**

**Operator outcome:** With the pinned GitHub Actions Source synchronized, an
operator starts the 12-case lexical evaluation in the browser and receives one
report showing every case's result, lexical Recall@10, ranking score, p50/p95
latency, and the corpus and evaluation-set versions used.

**Frontend surface:** An **Evaluations** screen lets the operator start the
evaluation and view its completed report.

**What proves it is done:** Starting evaluation for the synchronized pinned
Source returns a report for all 12 cases. Every case shows whether its expected
evidence appears in the first 10 results, its rank when present, and duration.
The report shows lexical summary metrics and safe failure feedback when the
required Source is unavailable or has not synchronized successfully.

**Boundaries:** The first M7 slice evaluates only the implemented lexical
method. Do not add vectors, embeddings, hybrid retrieval, reranking, chat, a
physical evaluation service, a separate database, or the M8 30-case release
evaluation set. M7 begins with M7T0 planning before implementation.

### What Proves M6T0 Is Done

- `CURRENT_FOCUS.md` contains the agreed M6 plan and all M6 delivery tasks.
- Each delivery task has a concrete user or operator scenario, observable
  proof, and explicit boundaries.
- M6 uses 12 reviewed cases now; M8 owns the 30-case release evaluation.

### M6T0 Boundaries

- M6T0 contains no product code, API change, database migration, or frontend
  behavior.
- M6T1 begins only after M6T0 is accepted.
