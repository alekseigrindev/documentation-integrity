# Current Focus

This file is the local working tracker for the active milestone.

## Previously Completed Milestones

- **M3 — Publisher management vertical slice — Completed 2026-09-02**
- **M4 — Source management vertical slice — Completed 2026-09-02**
- **M5 — Source synchronization vertical slice — Completed 2026-09-08**

## Active Milestone

**M6 — Measurable evidence retrieval vertical slice**

**Plain outcome:** A user searches synchronized documentation, sees passages
with citations, and can later rely on measured retrieval quality.

### M6 Agreed Delivery Plan

M6 first gives the user a usable cited lexical search. It then gives the
operator a reproducible comparison of retrieval methods before any method
replaces that lexical baseline.

M6 uses a reviewed evaluation set of 12 search cases. Each case records a user
query and the source locator or locators that count as correct evidence. The
30-case release evaluation remains M8 work.

A candidate retrieval method replaces lexical search only when it finds the
correct evidence in the first ten results at least as often as lexical search
and places correct evidence higher in the result list. The comparison also
records typical and slow search latency.

## M6 Task Tracker

| Task | Plain outcome | Status |
| --- | --- | --- |
| M6.0 Milestone definition | The team agrees the M6 outcome, acceptance evidence, delivery tasks, and boundaries before implementation begins. | Completed 2026-09-08 |
| M6.1 Citable lexical search in the browser | A user enters a documentation query and sees matching passages with their source citation. | Completed 2026-09-08 |
| M6.2 Measured retrieval selection | An operator compares retrieval methods on the agreed cases and retains the best measured method. | Planned |
| M6.3 Milestone finalization | The M6 user and operator stories work without known M6 defects or usability blockers. | Planned |

## Active Task

None. M6T1 is complete; begin M6T2 next.

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

**Operator scenario:** An operator runs one reproducible report that compares
lexical, vector, hybrid, and reranked retrieval on the agreed 12 search cases,
then sees which method remains selected and how long each method takes.

**What proves it is done:** The versioned report records, for every method,
whether expected evidence appears in the first ten results, how highly it is
ranked, typical latency, slow latency, and the selected method. A new method
replaces lexical only when it meets the agreed selection rule above.

**Boundaries:** No new user-facing search controls, diagnosis, chat, gRPC,
additional connectors, or release-quality claims. Building the 30-case release
evaluation set belongs to M8.

### M6T3 — Milestone finalization

**Proposed branch:** `feature/m6t3-retrieval-finalization`

**User and operator scenario:** After M6T1 and M6T2, the developer can search
synchronized documentation with citations, and the operator can run the agreed
retrieval comparison, without known M6 defects or usability blockers.

The operator can also inspect each successful ingestion run and see its added
and removed document and chunk counts.

**What proves it is done:** Every recorded M6 finding is resolved or explicitly
deferred, then the M6 browser search and 12-case retrieval comparison are run
again successfully. A controlled successful sync that adds and removes known
documents shows matching added and removed document and chunk counts in
**Ingestion runs**.

**Boundaries:** Include only fixes, small usability improvements, and
acceptance-evidence corrections discovered in M6. Do not add new retrieval
capabilities, change the agreed quality gate, add chat, or make unrelated
refactors. The ingestion improvement adds only four successful-run counters:
documents added, documents removed, chunks added, and chunks removed. Failed
runs continue to show a safe failure result without change counts; no
per-document audit history is added.

### What Proves M6T0 Is Done

- `CURRENT_FOCUS.md` contains the agreed M6 plan and all M6 delivery tasks.
- Each delivery task has a concrete user or operator scenario, observable
  proof, and explicit boundaries.
- M6 uses 12 reviewed cases now; M8 owns the 30-case release evaluation.

### M6T0 Boundaries

- M6T0 contains no product code, API change, database migration, or frontend
  behavior.
- M6T1 begins only after M6T0 is accepted.
