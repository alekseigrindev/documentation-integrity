# Model management

This project runs embedding and reranking models locally through ONNX Runtime.
Downloaded artifacts belong under the repository-root `models/` directory,
which is excluded by `.gitignore`. Each model has its own subdirectory
containing its ONNX graph and compatible tokenizer.

Do not commit model weights, tokenizers, absolute local paths, or a local
`.env` file. Commit the model identity, immutable upstream revision, expected
checksums, download procedure, and environment-variable names instead. This
keeps the repository small while making every local installation reproducible.

## Directory layout

```text
models/
├── nomic-embed-text-v1.5/
│   ├── model.onnx
│   └── tokenizer.json
└── bge-reranker-v2-m3/
    ├── model.onnx
    └── tokenizer.json
```

The application must load a model and tokenizer from the same model family and
compatible upstream revision. A tokenizer from another model can produce valid
tensor shapes while assigning different token IDs, causing incorrect results
without an obvious startup failure.

## Reranker: bge-reranker-v2-m3

The project uses the `bge-reranker-v2-m3` cross-encoder to score existing
query-passage pairs. The original model is published by BAAI under the
Apache-2.0 license. The ONNX artifact below is the ONNX Community conversion
of that model.

The local CPU configuration uses `model_quantized.onnx`. It is approximately
571 MB and contains its weights in one file. Do not download `onnx/model.onnx`
by itself: that graph requires the separate 2.27 GB `onnx/model.onnx_data`
file.

The model and tokenizer must come from the same pinned repository revision:

```text
Repository: onnx-community/bge-reranker-v2-m3-ONNX
Revision:   90213ffc6a8e6f051a6331269a0f5526cdd896f6
Model:      onnx/model_quantized.onnx
Tokenizer:  tokenizer.json
```

Source references:

- Original model and license: <https://huggingface.co/BAAI/bge-reranker-v2-m3>
- Pinned ONNX model: <https://huggingface.co/onnx-community/bge-reranker-v2-m3-ONNX/blob/90213ffc6a8e6f051a6331269a0f5526cdd896f6/onnx/model_quantized.onnx>
- Pinned tokenizer: <https://huggingface.co/onnx-community/bge-reranker-v2-m3-ONNX/blob/90213ffc6a8e6f051a6331269a0f5526cdd896f6/tokenizer.json>

### Download

Run these commands from the repository root. The URLs contain the immutable
revision rather than `main`, so repeating the procedure downloads the same
artifacts.

```bash
mkdir -p models/bge-reranker-v2-m3

RERANKER_REVISION=90213ffc6a8e6f051a6331269a0f5526cdd896f6

curl --fail --location --retry 3 --continue-at - \
  "https://huggingface.co/onnx-community/bge-reranker-v2-m3-ONNX/resolve/${RERANKER_REVISION}/onnx/model_quantized.onnx?download=true" \
  --output models/bge-reranker-v2-m3/model.onnx

curl --fail --location --retry 3 --continue-at - \
  "https://huggingface.co/onnx-community/bge-reranker-v2-m3-ONNX/resolve/${RERANKER_REVISION}/tokenizer.json?download=true" \
  --output models/bge-reranker-v2-m3/tokenizer.json
```

`--continue-at -` resumes an interrupted download when a partial destination
file already exists. `--fail` makes HTTP errors fail the command instead of
being saved as model files.

### Verify

Verify both downloads before configuring the application:

```bash
shasum -a 256 \
  models/bge-reranker-v2-m3/model.onnx \
  models/bge-reranker-v2-m3/tokenizer.json
```

Expected output:

```text
912fc1215c2dbff6499700534bd8d31253af01573861abbfc43afd1fab6cce5d  models/bge-reranker-v2-m3/model.onnx
8bf8afbfd11306bd872018c53bfdf2e160a56f8edbcf49933324404791c148d3  models/bge-reranker-v2-m3/tokenizer.json
```

If either checksum differs, remove only the mismatched local file and download
it again. Do not start the application with an unverified artifact.

### Application paths

Application configuration points to these two files:

```text
<repository-root>/models/bge-reranker-v2-m3/model.onnx
<repository-root>/models/bge-reranker-v2-m3/tokenizer.json
```

Store machine-specific absolute paths only in the ignored local `.env` file.
Commit environment-variable placeholders or `.env.example` entries instead of
the local paths themselves.
