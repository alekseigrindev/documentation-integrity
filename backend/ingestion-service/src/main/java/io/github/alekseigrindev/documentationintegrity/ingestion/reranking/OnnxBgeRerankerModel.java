package io.github.alekseigrindev.documentationintegrity.ingestion.reranking;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.util.PairList;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;

public class OnnxBgeRerankerModel implements TextRerankingModel, AutoCloseable {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(OnnxBgeRerankerModel.class);

    private static final Set<String> EXPECTED_INPUTS = Set.of("input_ids", "attention_mask");
    private static final Set<String> EXPECTED_OUTPUT = Set.of("logits");

    private final OrtEnvironment environment;
    private final HuggingFaceTokenizer tokenizer;
    private final OrtSession session;

    private final int batchSize;


    public OnnxBgeRerankerModel(RerankerProperties properties) {
        requiredReadableFile(properties.modelPath(), "ONNX reranking model");
        requiredReadableFile(properties.tokenizerPath(), "reranker tokenizer");
        this.batchSize = properties.batchSize();
        this.environment = OrtEnvironment.getEnvironment();
        this.tokenizer = loadTokenizer(properties);
        this.session = loadSession(properties.modelPath());

        validateRerankerContract();
    }

    private void validateRerankerContract() {
        Set<String> actualInputs = session.getInputNames();
        Set<String> actualOutputs = session.getOutputNames();

        if (!actualInputs.equals(EXPECTED_INPUTS)) {
            close();
            throw new IllegalStateException(
                    "Unexpected ONNX reranker model inputs: " + actualInputs
            );
        }

        if (!actualOutputs.equals(EXPECTED_OUTPUT)) {
            close();
            throw new IllegalStateException(
                    "Missing ONNX model output: " + EXPECTED_OUTPUT
            );
        }
    }

    private OrtSession loadSession(Path path) {
        try (OrtSession.SessionOptions options =
                     new OrtSession.SessionOptions()) {
            return environment.createSession(
                    path.toString(),
                    options
            );
        } catch (OrtException e) {
            tokenizer.close();
            throw new IllegalStateException(
                    "Unable to load the ONNX reranker model", e
            );
        }
    }

    private HuggingFaceTokenizer loadTokenizer(RerankerProperties properties) {
        try {
            return HuggingFaceTokenizer.builder()
                    .optTokenizerPath(properties.tokenizerPath())
                    .optPadding(true)
                    .optTruncation(true)
                    .optMaxLength(properties.maxTokens())
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to load the reranking tokenizer",
                    e
            );
        }
    }

    private void requiredReadableFile(Path path, String description) {
        if (path == null
                || !Files.isRegularFile(path)
                || !Files.isReadable(path)) {
            throw new IllegalArgumentException(
                    description + " is not readable file: " + path
            );
        }
    }

    @Override
    public float[] score(List<RerankingInput> inputs) {
        if (inputs == null) {
            throw new IllegalArgumentException("Reranking inputs must not be null");
        }

        float[] scores = new float[inputs.size()];

        for (int from = 0; from < inputs.size(); from += batchSize) {
            int to = Math.min(from + batchSize, inputs.size());
            float[] batchScores = scoreBatch(inputs.subList(from, to));

            System.arraycopy(batchScores, 0, scores, from, batchScores.length);
        }

        return scores;
    }

    private float[] scoreBatch(List<RerankingInput> batch) {
        PairList<String, String> pairs = new PairList<>(batch.size());

        for (RerankingInput input : batch) {
            checkInput(input);
            pairs.add(input.query().strip(), input.passage().strip());
        }

        Encoding[] encodings = tokenizer.batchEncode(pairs);

        if (encodings.length != batch.size()) {
            throw new IllegalStateException(
                    "Tokenizer returned an unexpected number of encodings"
            );
        }

        long[][] inputIds = new long[encodings.length][];
        long[][] attentionMasks = new long[encodings.length][];

        for (int i = 0; i < encodings.length; i++) {
            inputIds[i] = encodings[i].getIds();
            attentionMasks[i] = encodings[i].getAttentionMask();
        }

        try (
                OnnxTensor idsTensor = OnnxTensor.createTensor(environment, inputIds);
                OnnxTensor maskTensor = OnnxTensor.createTensor(environment, attentionMasks);
        ) {
            Map<String, OnnxTensor> tensors = Map.of(
                    "input_ids", idsTensor,
                    "attention_mask", maskTensor
            );

            try (OrtSession.Result result = session.run(tensors)) {
                OnnxValue output = result.get("logits")
                        .orElseThrow(() -> new IllegalStateException(
                                "ONNX reranker did not return logits"
                        ));

                Object value = output.getValue();

                if (!(value instanceof float[][] logits)
                        || logits.length != batch.size()) {
                    throw new IllegalStateException(
                            "Unexpected ONNX reranker output shape"
                    );
                }

                float[] scores = new float[logits.length];

                for (int i = 0; i < logits.length; i++) {
                    if (logits[i].length != 1
                            || !Float.isFinite(logits[i][0])) {
                        throw new IllegalStateException(
                                "Invalid reranker score at batch position " + i
                        );
                    }

                    scores[i] = logits[i][0];
                }

                return scores;
            }
        } catch (OrtException e) {
            throw new IllegalStateException(
                    "Unable to run ONNX reranker inference",
                    e
            );
        }
    }

    private void checkInput(RerankingInput input) {
        if (input == null
                || input.query() == null
                || input.query().isBlank()
                || input.passage() == null
                || input.passage().isBlank())
            throw new IllegalArgumentException(
                    "Every reranking input needs a query and passage"
            );
    }


    @Override
    public void close() {
        try {
            session.close();
        } catch (OrtException e) {
            throw new IllegalStateException(
                    "Unable to close the ONNX reranker model",
                    e
            );
        } finally {
            tokenizer.close();
        }
    }

}
