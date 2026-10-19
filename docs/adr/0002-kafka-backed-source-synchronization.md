# ADR 0002: Kafka-Backed Source Synchronization for the MVP

- Status: Proposed in M10T0; implementation not started
- Date: 2026-10-19

## Context

Sync currently executes inside an ingestion-service HTTP request. Concurrent
Source runs compete for local inference resources; local operation has produced OOM.
The operator needs a prompt acceptance response, serial processing, visible
status, and recovery of accepted work after application restart.

Kafka is selected for the M10 cross-process handoff. Current throughput alone
does not require a broker; a database-only queue remains a viable simpler
alternative. The selected boundary does not imply a measured performance gain.

## Decision

Use the existing api-service as the public synchronization boundary. It sends
a small versioned command containing a stable request/run ID and Source ID to
Kafka. The browser generates the request ID before submission so it can reuse
it even if no response arrives after an ambiguous publication timeout. Return an
acceptance receipt only after broker acknowledgement; do not wait for scanning.
No document content, credentials, or model objects belong in the command.
The Source ID is the message key.

Ingestion-service remains the sole owner of Source and ingestion persistence.
A short Kafka listener durably registers a queued run under the command's ID
before committing the offset. It must tolerate registration commit followed by
crash before offset commit. The existing run record acts as the durable work
record; avoid an additional generic job/inbox table without demonstrated need.

One serial processor in the same ingestion-service processes queued database
runs using the existing synchronization behavior. It must not hold a database
transaction open merely while waiting for work. Do not put a long ONNX-backed
scan inside the Kafka registration listener or build an unbounded in-memory
executor backlog. Terminal records make duplicate delivery a no-op. Conflicting
requests for one active Source are rejected visibly. On restart recover an
interrupted run under its original ID with a bounded attempt count. This may
replay document work; it is not exactly-once execution or whole-Source rollback.

api-service delegates run/source reads needed by this flow over internal HTTP,
with bounded timeouts; it neither imports ingestion JPA code nor accesses its
tables. The acceptance receipt may precede run registration, so the frontend
represents that pending state. Validation rejection for a usable request ID
must remain observable, not become a permanent missing run.

Use a minimal contracts module for the command envelope and its version.
Use one command topic, one consumer group, and one dead-letter topic for
unprocessable commands. Use bounded registration retries and processing
attempts. Configure finite retention/storage and preserve broker data across
ordinary local container recreation. A single local broker is not a guarantee
against disk loss, broker-volume deletion, or cluster failures.

## Alternatives

- **Synchronous HTTP:** easiest, but does not deliver prompt acceptance or
  durable queued handoff.
- **In-process queue:** can serialize work cheaply but loses waiting work
  on restart unless persisted; it does not provide cross-process delivery.
- **Database queue only:** viable and operationally simpler for the local
  product. Kafka adds broker operation but separates API publication from
  ingestion registration using a versioned transport contract. This is an
  explicit architecture trade-off, not a claim that the simpler option cannot work.
- **API writes a run and publishes separately:** creates a two-write failure
  gap. Instead, API publishes only; ingestion registers from durable delivery.
  No producer outbox is needed because there is no producer database write.
- **Scan in the Kafka listener:** fewer parts but couples long inference work
  to consumer polling and hides later queued jobs until a scan finishes.
  Short registration plus one processor separates visibility from execution.
- **Separate worker service/distributed transaction:** unnecessary for one
  local processor; neither is included.

## Consequences and Limits

- POST changes from completed-run `201 Created` to receipt `202 Accepted`;
  frontend and API changes ship together. Run reads keep their purpose.
- Admission is not success. Kafka delivery and database registration are
  eventually consistent; the browser must not label a receipt SUCCEEDED.
- Retry an ambiguous response with the same ID. New IDs are new requests,
  not evidence of transport redelivery.
- One ingestion-service instance and one processor are supported. Kafka
  partitions alone do not serialize a separate database processor. Multiple
  replicas, leases, and horizontal scaling are out of scope.
- Serial synchronization reduces simultaneous inference load but imposes no
  CPU/RAM quota and does not serialize interactive search with ingestion.
- Existing current-state and failed-scan limitations remain; no snapshots or
  automatic rollback of an entire Source are introduced.
- Manual Refresh is sufficient. No polling framework, progress dashboard,
  generic scheduler, cancellation, or DLT replay UI is added.

## Required Evidence

M10T1 in CURRENT_FOCUS.md proves prompt browser acceptance, queued visibility,
serial execution, duplicate delivery, safe Source failure, and broker-unavailable
submission. Stable IDs and durable registration are implemented in T1.

M10T2 separately proves application-restart recovery, bounded retries, and
malformed-message isolation. Preserve document identity, terminal counts, and
failed-scan deletion safety across interruption. Do not claim these recovery
guarantees at T1 completion; the milestone requires both tasks. M10T3 contains
only small discovered corrections and final verification.

## Reconsideration Triggers

Revisit the single-instance processor when multiple replicas are required.
Revisit admission/backpressure when local queue storage bounds are insufficient.
Introduce an outbox only if a producer must atomically change its own database
and publish an event.
