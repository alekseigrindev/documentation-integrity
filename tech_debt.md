# Technical Debt

## M8T1 — Real-model embedding smoke test

**Deferred:** Add a local smoke test for `OnnxNomicEmbeddingModel` that loads
the downloaded Nomic ONNX model and tokenizer, executes both document and query
embedding, and verifies:

- every returned embedding has 768 dimensions;
- every returned embedding is L2-normalized to a length close to `1`;
- a workflow query is more similar to a related workflow passage than to a
  clearly unrelated passage.

**Why deferred:** M8T1 implementation is continuing before this automated
runtime compatibility check is added. The downloaded model artifacts remain
outside Git, so the test must be explicitly enabled only where their configured
paths are available.

**Consequence:** Compilation proves the Java and library contracts, but it does
not prove that the pinned model, tokenizer, native ONNX Runtime, and pooling
pipeline execute together correctly on local hardware. Do not claim automated
real-model inference evidence until this test executes without being skipped.

**Resolution evidence:** A focused test executes one document batch and one
query through the real model, passes the three assertions above, and the test
run reports it as executed rather than skipped.
