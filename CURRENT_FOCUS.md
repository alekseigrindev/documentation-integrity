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

## Active Milestone

**M7 — Evaluation vertical slice**

**Plain outcome:** A user searches all synchronized documentation or selected
Sources in the browser. An operator then selects the synchronized pinned GitHub
Actions Source, runs the fixed 12-case lexical evaluation, and receives one
readable report showing every case and the overall measured result.

## M7 Task Tracker

| Task | Plain outcome | Status |
| --- | --- | --- |
| M7.0 Milestone definition | The team agrees the M7 outcome, evidence, delivery tasks, measurements, and boundaries before implementation begins. | Completed 2026-09-11 |
| M7.1 Source-scoped lexical search and evaluation | A user searches all or selected Sources, and an operator runs the 12 fixed cases for one synchronized Source. | Completed 2026-09-16 |
| M7.2 Milestone finalization | The M7 evaluation story works after small fixes and usability improvements found during delivery. | In progress |

## Active Task

**M7T2 — Milestone finalization**

### M7T0 — Milestone definition

**Proposed branch:** `feature/m7t0-milestone-definition`

**What proves it is done:** This tracker defines one source-scoped lexical
evaluation story, its 12-case input, its report measurements, its delivery
tasks, and its exclusions. M7T0 contains no product implementation.

### M7T1 — Source-scoped lexical search and evaluation

**Proposed branch:** `feature/m7t1-browser-lexical-evaluation`

**User and operator scenario:** A user searches all synchronized documentation
or selects one or more Sources to limit a lexical search to those Sources. An
operator selects one synchronized Source, clicks **Run lexical evaluation**,
and sees a report for the fixed 12 GitHub Actions cases. For each query, the
report shows the expected locator, whether it is found in the first 10 results,
its first rank when found, and its duration. The summary shows Recall@10,
MRR@10, p50 latency, p95 latency, the evaluated Source, and the evaluation-set
version. The operator can download that current report as a JSON file.

**What proves it is done:** A browser search with no Source selection searches
the full corpus; a search with selected Sources returns passages only from
those Sources. With the pinned GitHub Actions Source synchronized, one browser
action produces a 12-case report scoped to that Source. A known case shows its
expected locator and rank. Downloading that report produces a JSON file with
the same Source, evaluation-set version, case results, and summary. An unknown
Source and a Source with no successful synchronization produce safe failure
feedback.

**Boundaries:** The existing search accepts an optional list of Source IDs;
omitting it means the full corpus. The evaluation request names one `sourceId`,
and every evaluation query is limited to that Source's documents. The report is
returned for the current request only: do not add an evaluation database table
or historical report storage. A browser download of the current JSON report is
allowed and does not create server-side history. Package the reviewed fixed
case data for runtime use. Do not add vectors, embeddings, hybrid retrieval,
reranking, chat, a separate deployed evaluation service, or the M8 30-case
release set.

**Measurements:** Recall@10 is the share of cases whose expected locator
appears in the first 10 results. MRR@10 is the average of `1 / first matching
rank`, with zero for a case whose expected locator is absent from the first 10.
Latency is measured for each lexical query; p50 and p95 summarize the 12
durations.

**Task completion — 2026-09-16:** A real browser evaluation against the
synchronized local GitHub Actions Source produced the downloadable evaluation
set v1 lexical baseline: Recall@10 `0.25`, MRR@10 `0.13888888888888887`, p50
`2 ms`, and p95 `29 ms`. Frontend tests, lint, and production build passed.
Backend automated evaluation coverage was explicitly deferred by Aleksei and
is not claimed as completed evidence.

### M7T2 — Milestone finalization

**Proposed branch:** `feature/m7t2-evaluation-finalization`

**Purpose:** Implement only small fixes, usability improvements, and
acceptance-evidence corrections explicitly noticed during M7T1. Re-run the
12-case browser evaluation story after those corrections.

### Recorded Corrections

1. **Collapsible long search passages.** Long search results start with a
   compact preview and provide **Expand** and **Collapse** controls. Short
   results remain fully visible without an unnecessary control.

   **Proof:** A search returning a passage longer than the preview limit shows
   shortened content, expands to the complete passage, and collapses again
   without hiding its citation.

2. **Compact search controls.** Multi-Source selection stays compact as the
   Source list grows, redundant explanatory text is removed, and the result
   count appears with the results instead of as standalone content.

   **Proof:** The closed Source selector shows **All Sources** or the selected
   count and opens a scrollable checkbox menu; the query prompt appears inside
   the input; and a completed search shows its passage count beside the
   **Results** heading. Clicking outside an open Source menu closes it.

**Boundaries:** Do not use finalization to add a new retrieval method, change
the evaluation set, persist report history, or make unrelated refactors.

## Completed Milestone Record

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
