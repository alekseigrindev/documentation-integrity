package io.github.alekseigrindev.documentationintegrity.ingestion.embedding;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OnnxNomicEmbeddingModel implements TextEmbeddingModel, AutoCloseable {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(OnnxNomicEmbeddingModel.class);

    private static final Set<String> EXPECTED_INPUTS = Set.of(
            "input_ids",
            "token_type_ids",
            "attention_mask"
    );

    private static final String EXPECTED_OUTPUT = "last_hidden_state";

    private static final String DOCUMENT_PREFIX = "search_document: ";
    private static final String QUERY_PREFIX = "search_query: ";

    private static final int EMBEDDING_DIMENSIONS = 768;

    private final OrtEnvironment environment;
    private final HuggingFaceTokenizer tokenizer;
    private final OrtSession session;
    private final int batchSize;


    public OnnxNomicEmbeddingModel(EmbeddingProperties properties) {
        requireReadableFile(properties.modelPath(), "ONNX model");
        requireReadableFile(properties.tokenizerPath(), "tokenizer");
        this.batchSize = properties.batchSize();

        this.environment = OrtEnvironment.getEnvironment();
        this.tokenizer = loadTokenizer(properties);
        this.session = loadSession(properties.modelPath());

        validateModelContract();
    }

    @Override
    public List<float[]> embedDocuments(
            List<String> texts
    ) {

        if (texts == null || texts.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one document text must be provided"
            );
        }

        List<float[]> embeddings = new ArrayList<>(texts.size());

        int totalBatches =
                (texts.size() + batchSize - 1) / batchSize;

        long embeddingStartedAt = System.nanoTime();

        LOGGER.info(
                "Embedding {} chunks in {} batches with batch size {}",
                texts.size(),
                totalBatches,
                batchSize
        );

        for (int fromIndex = 0;
             fromIndex < texts.size();
             fromIndex += batchSize) {

            int toIndex = Math.min(
                    fromIndex + batchSize,
                    texts.size()
            );

            List<String> batchTexts = texts.subList(
                    fromIndex,
                    toIndex
            );

            embeddings.addAll(
                    embedBatch(DOCUMENT_PREFIX, batchTexts)
            );

            int completedBatch = fromIndex / batchSize + 1;

            if (completedBatch == 1
                    || completedBatch % 10 == 0
                    || completedBatch == totalBatches) {

                LOGGER.info(
                        "Embedding progress: batch {}/{}, chunks completed {}/{}",
                        completedBatch,
                        totalBatches,
                        toIndex,
                        texts.size()
                );
            }
        }

        LOGGER.info(
                "Embedded {} chunks in {} ms",
                texts.size(),
                TimeUnit.NANOSECONDS.toMillis(
                        System.nanoTime() - embeddingStartedAt
                )
        );

        return List.copyOf(embeddings);
    }


    @Override
    public float[] embedQuery(String query) {
        return embedBatch(
                QUERY_PREFIX,
                List.of(query)
        ).getFirst();
    }


    private List<float[]> embedBatch(
            String prefix,
            List<String> texts
    ) {
        TokenizedBatch batch = tokenize(prefix, texts);

        float[][][] hiddenStates = runInference(batch);

        return poolAndNormalize(
                hiddenStates,
                batch.attentionMasks()
        );
    }

    private List<float[]> poolAndNormalize(
            float[][][] hiddenStates,
            long[][] attentionMask
    ) {
        if (hiddenStates.length != attentionMask.length) {
            throw new IllegalArgumentException(
                    "Mismatched number of hidden states and attention masks"
            );
        }

        List<float[]> embeddings =
                new ArrayList<>(hiddenStates.length);

        for (int batchIndex = 0;
             batchIndex < hiddenStates.length;
             batchIndex++) {

            float[][] tokenVectors = hiddenStates[batchIndex];
            long[] mask = attentionMask[batchIndex];

            if (tokenVectors.length != mask.length) {
                throw new IllegalArgumentException(
                        "Mismatched number of token vectors and mask for batch " + batchIndex
                );
            }

            double[] sums = new double[EMBEDDING_DIMENSIONS];

            int includedTokens = 0;

            for (int tokenIndex = 0;
                 tokenIndex < tokenVectors.length;
                 tokenIndex++) {

                if (mask[tokenIndex] == 0) {
                    continue;
                }

                float[] tokenVector = tokenVectors[tokenIndex];

                if (tokenVector.length != EMBEDDING_DIMENSIONS) {
                    throw new IllegalStateException(
                            "Unexpected embedding dimension: "
                                    + tokenVector.length
                    );
                }

                for (int dimension = 0;
                     dimension < EMBEDDING_DIMENSIONS;
                     dimension++) {
                    sums[dimension] += tokenVector[dimension];
                }

                includedTokens++;
            }

            if (includedTokens == 0) {
                throw new IllegalStateException(
                        "ONNX output contains no non-padding tokens"
                );
            }
            embeddings.add(normalizeMean(sums, includedTokens));
        }

        return List.copyOf(embeddings);
    }

    private float[] normalizeMean(
            double[] sums,
            int includedTokens
    ) {
        double[] means = new double[EMBEDDING_DIMENSIONS];
        double squaredLength = 0;

        for (int dimension = 0;
             dimension < EMBEDDING_DIMENSIONS;
             dimension++) {

            double mean = sums[dimension] / includedTokens;
            means[dimension] = mean;
            squaredLength += mean * mean;
        }

        double length = Math.sqrt(squaredLength);

        if (length == 0) {
            throw new IllegalStateException(
                    "ONNX model produced a zero-length embedding"
            );
        }

        float[] normalized = new float[EMBEDDING_DIMENSIONS];

        for (int dimension = 0;
             dimension < EMBEDDING_DIMENSIONS;
             dimension++) {
            normalized[dimension] =
                    (float) (means[dimension] / length);
        }

        return normalized;
    }

    private void validateModelContract() {
        Set<String> actualInputs = session.getInputNames();
        Set<String> actualOutputs = session.getOutputNames();

        if (!actualInputs.equals(EXPECTED_INPUTS)) {
            close();
            throw new IllegalStateException(
                    "Unexpected ONNX model inputs: " + actualInputs
            );
        }

        if (!actualOutputs.contains(EXPECTED_OUTPUT)) {
            close();
            throw new IllegalStateException(
                    "Missing ONNX model output: " + EXPECTED_OUTPUT
            );
        }
    }

    private OrtSession loadSession(Path modelPath) {
        try (OrtSession.SessionOptions options =
                     new OrtSession.SessionOptions()) {
            return environment.createSession(
                    modelPath.toString(),
                    options
            );
        } catch (OrtException exception) {
            tokenizer.close();
            throw new IllegalStateException(
                    "Unable to load the ONNX embedding model",
                    exception
            );
        }
    }

    private HuggingFaceTokenizer loadTokenizer(EmbeddingProperties properties) {
        try {
            return HuggingFaceTokenizer.builder()
                    .optTokenizerPath(properties.tokenizerPath())
                    .optPadding(true)
                    .optTruncation(true)
                    .optMaxLength(properties.maxTokens())
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to load the embedding tokenizer",
                    e
            );
        }
    }

    private void requireReadableFile(Path path, String description) {
        if (path == null
                || !Files.isRegularFile(path)
                || !Files.isReadable(path)) {
            throw new IllegalArgumentException(
                    description + " is not a readable file: " + path
            );
        }
    }

    @Override
    public void close() {
        try {
            session.close();
        } catch (OrtException e) {
            throw new IllegalStateException(
                    "Unable to close the ONNX embedding model",
                    e
            );
        } finally {
            tokenizer.close();
        }
    }

    private TokenizedBatch tokenize(
            String prefix,
            List<String> texts
    ) {
        if (texts == null || texts.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one text must be provided"
            );
        }

        List<String> prefixedTexts = texts.stream()
                .map(text -> prefix + requireText(text))
                .toList();

        Encoding[] encodings = tokenizer.batchEncode(prefixedTexts);

        long[][] inputIds = new long[encodings.length][];
        long[][] tokenTypeIds = new long[encodings.length][];
        long[][] attentionMasks = new long[encodings.length][];

        for (int i = 0; i < encodings.length; i++) {
            inputIds[i] = encodings[i].getIds();
            tokenTypeIds[i] = encodings[i].getTypeIds();
            attentionMasks[i] = encodings[i].getAttentionMask();
        }

        return new TokenizedBatch(inputIds, tokenTypeIds, attentionMasks);
    }

    private float[][][] runInference(TokenizedBatch batch) {
        try (
                OnnxTensor inputIds = OnnxTensor.createTensor(
                        environment,
                        batch.inputIds()
                );
                OnnxTensor tokenTypeIds = OnnxTensor.createTensor(
                        environment,
                        batch.tokenTypeIds()
                );
                OnnxTensor attentionMasks = OnnxTensor.createTensor(
                        environment,
                        batch.attentionMasks()
                );
        ) {
            Map<String, OnnxTensor> inputs = Map.of(
                    "input_ids", inputIds,
                    "token_type_ids", tokenTypeIds,
                    "attention_mask", attentionMasks
            );

            try (OrtSession.Result result = session.run(inputs)) {
                OnnxValue output = result.get(EXPECTED_OUTPUT)
                        .orElseThrow(() -> new IllegalStateException(
                                "ONNX model did not return "
                                        + EXPECTED_OUTPUT
                        ));

                Object value = output.getValue();

                if (!(value instanceof float[][][] hiddenStates)) {
                    throw new IllegalStateException(
                            "Unexpected ONNX output type: "
                                    + value.getClass().getName()
                    );
                }

                return hiddenStates;
            }
        } catch (OrtException e) {
            throw new IllegalStateException(
                    "Unable to run ONNX embedding inference",
                    e
            );
        }
    }

    private String requireText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Text must not be null or blank"
            );
        }

        return text.strip();
    }

    private record TokenizedBatch(
            long[][] inputIds,
            long[][] tokenTypeIds,
            long[][] attentionMasks
    ) {
    }
}
