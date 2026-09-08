# Current Focus

This file is the local working tracker for the active milestone.

## Previously Completed Milestones

- **M3 — Publisher management vertical slice — Completed 2026-09-02**
- **M4 — Source management vertical slice — Completed 2026-09-02**
- **M5 — Source synchronization vertical slice — Completed 2026-09-08**

## Active Milestone

No active milestone. Select the next milestone before starting new work.

M5 is complete. An operator can synchronize a registered Source, see the
result safely, and review recorded ingestion runs.

## M5 Task Tracker

| Task | Plain outcome | Status |
| --- | --- | --- |
| M5.1 Reliable local synchronization | The system can synchronize the approved local GitHub Actions fixture without corrupting the current documentation state. | Completed 2026-09-08 |
| M5.2 Synchronization button and result | The operator can start synchronization from the Sources screen, see the result, and review recent runs. | Completed 2026-09-08 |

## Active Task

None.

### What M5.1 Must Make True

We use one explicitly allowed local GitHub Actions fixture. When the system
synchronizes its registered Source:

1. The first run creates the fixture's documentation in the current state.
2. A second unchanged run does not create duplicate documents or chunks.
3. A changed fixture file replaces the old searchable content.
4. A fixture file removed before a complete run is removed from current state.
5. A broken or incomplete run leaves the last successful documents searchable
   and records a failed run with a useful, bounded error.

### What Proves M5.1 Is Done

- Backend integration tests run the five scenarios above against the licensed
  fixture and database.
- The tests show that no duplicate or stale current documents remain after a
  successful run, and that a failed run does not remove good existing data.
- A start request leaves a final run record that says either `SUCCEEDED` or
  `FAILED`; a failed run includes a short operator-safe reason.

### M5.1 Boundaries

- Keep the existing synchronous local HTTP path.
- Do not add Kafka, background workers, retries, remote crawling, new connector
  types, retrieval, diagnosis, or chat.

### M5 operational logging

Each synchronization attempt must emit safe lifecycle logs for the Source ID,
created run ID, completed scan document count, and final success or failure.
Failure logs may include the exception type and bounded message, but never
document content, raw files, or arbitrary local-directory paths.

### Current local-directory decision

A `LOCAL_DIRECTORY` Source stores the operator-provided `file:` URL of a
directory accessible to `ingestion-service`. Registering that Source is the
explicit allowlisting action. The browser UI accepts a local or mounted-network
directory path and can ask the local backend to open a directory picker. This
works only when the browser and `ingestion-service` run on the same desktop
machine; it does not use an environment root.

## Planned Next Task

**M5.2 — Synchronization button and result**

**Proposed branch:** `feature/m5t2-synchronization-button-and-result`

### What M5.2 Must Make True

On the Sources screen, the operator can select a registered Source and press
**Sync**. While the request runs, the button shows that synchronization is in
progress. When it finishes, the screen shows either:

- **Succeeded** — the Source was synchronized; or
- **Failed** — a short safe explanation of why it failed.

The operator can also open **Ingestion runs** to see recent runs across Sources,
with their Source name, status, times, and failed-run message when applicable,
and can refresh the list on demand.

### What Proves M5.2 Is Done

- In a real local browser, the operator starts synchronization for the allowed
  fixture Source, sees the successful result, and can view ingestion runs.
- Frontend lint and production build pass.

Focused frontend tests are deliberately deferred for this MVP closure.

### M5.2 Boundaries

- Do not add Source editing or deletion, filtering, pagination, run-detail
  pages, polling, global state, a router, Kafka, retrieval, diagnosis, or chat.

## What Proves All of M5 Is Done

M5 is complete when the real local browser proves this complete story:

1. A registered GitHub Source appears in Sources.
2. The operator clicks **Sync** and sees that it is running.
3. The system finishes and the operator sees **Succeeded**.
4. The backend tests prove that repeated, changed, removed, and failed fixture
   runs keep the documentation state correct and safe.

At that point, the next milestone may start. Nothing about Kafka is an M5
acceptance condition.
