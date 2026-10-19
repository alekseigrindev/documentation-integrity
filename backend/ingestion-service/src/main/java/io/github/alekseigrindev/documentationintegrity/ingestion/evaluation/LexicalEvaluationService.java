package io.github.alekseigrindev.documentationintegrity.ingestion.evaluation;

import io.github.alekseigrindev.documentationintegrity.ingestion.run.IngestionRunRepository;
import io.github.alekseigrindev.documentationintegrity.ingestion.run.IngestionRunStatus;
import io.github.alekseigrindev.documentationintegrity.ingestion.search.DocumentationSearchHit;
import io.github.alekseigrindev.documentationintegrity.ingestion.search.DocumentationSearchService;
import io.github.alekseigrindev.documentationintegrity.ingestion.search.RetrievalMethod;
import io.github.alekseigrindev.documentationintegrity.ingestion.search.RetrievalProperties;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.SourceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LexicalEvaluationService {

    private static final int RESULT_LIMIT = 10;

    private static final Logger LOGGER = LoggerFactory.getLogger(LexicalEvaluationService.class);

    private final SourceRepository sourceRepository;
    private final IngestionRunRepository ingestionRunRepository;
    private final DocumentationSearchService documentationSearchService;
    private final EvaluationSetLoader evaluationSetLoader;
    private final RetrievalProperties retrievalProperties;

    public LexicalEvaluationReport evaluate(
            UUID sourceId,
            RetrievalMethod retrievalMethod
    ) {

        requireEvaluableSource(sourceId);

        EvaluationSet evaluationSet = evaluationSetLoader.load();

        LOGGER.info(
                "Evaluation started: method={}, sourceId={}. cases={}",
                retrievalMethod,
                sourceId,
                evaluationSet.cases().size()
        );

        List<LexicalEvaluationCaseResult> caseResults = evaluationSet.cases().stream()
                .map(evaluationCase -> evaluateCase(
                        sourceId,
                        evaluationCase,
                        retrievalMethod
                ))
                .toList();

        LOGGER.info(
                "Evaluation completed: method={}, sourceId={}, cases={}",
                retrievalMethod,
                sourceId,
                caseResults.size()
        );

        return new LexicalEvaluationReport(
                sourceId,
                evaluationSet.evaluationSetVersion(),
                retrievalMethod,
                retrievalProperties,
                caseResults,
                summarize(caseResults)
        );

    }

    private LexicalEvaluationSummary summarize(
            List<LexicalEvaluationCaseResult> caseResults
    ) {
        int totalCases = caseResults.size();

        if (totalCases == 0) {
            return new LexicalEvaluationSummary(
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0
            );
        }

        int casesFoundAtTen = Math.toIntExact(
                caseResults.stream()
                        .filter(result ->
                                result.firstMatchingRank() != null
                        )
                        .count()
        );

        double hitRateAtTen =
                (double) casesFoundAtTen / totalCases;

        double meanPrecisionAtTen = caseResults.stream()
                .mapToDouble(
                        LexicalEvaluationCaseResult::precisionAtTen
                )
                .average()
                .orElse(0);

        double meanRecallAtTen = caseResults.stream()
                .mapToDouble(
                        LexicalEvaluationCaseResult::recallAtTen
                )
                .average()
                .orElse(0);

        double mrrAtTen = caseResults.stream()
                .mapToDouble(result ->
                        result.firstMatchingRank() == null
                                ? 0
                                : 1.0 / result.firstMatchingRank()
                )
                .average()
                .orElse(0);

        double meanNdcgAtTen = caseResults.stream()
                .mapToDouble(LexicalEvaluationCaseResult::ndcgAtTen)
                .average()
                .orElse(0);

        List<Long> sortedDurations = caseResults.stream()
                .map(LexicalEvaluationCaseResult::durationMs)
                .sorted()
                .toList();

        return new LexicalEvaluationSummary(
                totalCases,
                casesFoundAtTen,
                hitRateAtTen,
                meanPrecisionAtTen,
                meanRecallAtTen,
                mrrAtTen,
                meanNdcgAtTen,
                percentile(sortedDurations, 0.50),
                percentile(sortedDurations, 0.95)
        );
    }

    private long percentile(
            List<Long> sortedValues,
            double percentile
    ) {
        int index = (int) Math.ceil(
                percentile * sortedValues.size()
        ) - 1;

        return sortedValues.get(Math.max(index, 0));
    }

    private LexicalEvaluationCaseResult evaluateCase(
            UUID sourceId,
            EvaluationCase evaluationCase,
            RetrievalMethod retrievalMethod
    ) {

        LOGGER.info(
                "Evaluation case started: caseId={}, method={}",
                evaluationCase.id(),
                retrievalMethod
        );

        long startedAt = System.nanoTime();

        List<DocumentationSearchHit> searchHits = documentationSearchService.search(
                evaluationCase.query(),
                Set.of(sourceId),
                retrievalMethod);

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);

        List<Integer> rankedRelevances = searchHits.stream()
                .limit(RESULT_LIMIT)
                .map(hit -> relevanceOf(
                        hit,
                        evaluationCase.expectedPassages()
                ))
                .toList();

        int relevantPassagesAtTen = Math.toIntExact(
                rankedRelevances.stream()
                        .filter(relevance -> relevance == 1)
                        .count()
        );

        if (evaluationCase.expectedPassages().isEmpty()) {
            throw new IllegalStateException(
                    "Evaluation case '%s' has no expected passages"
                            .formatted(evaluationCase.id())
            );
        }

        double precisionAtTen =
                (double) relevantPassagesAtTen / RESULT_LIMIT;

        double recallAtTen =
                (double) relevantPassagesAtTen
                        / evaluationCase.expectedPassages().size();

        LOGGER.info(
                "Evaluation case search completed: caseId={}, hits={}, durationMs={}",
                evaluationCase.id(),
                searchHits.size(),
                durationMs
        );

        return new LexicalEvaluationCaseResult(
                evaluationCase.id(),
                evaluationCase.query(),
                evaluationCase.expectedPassages(),
                relevantPassagesAtTen,
                precisionAtTen,
                recallAtTen,
                ndcgAtTen(
                        rankedRelevances,
                        evaluationCase.expectedPassages().size()
                ),
                firstMatchingRank(rankedRelevances),
                durationMs
        );
    }

    private int relevanceOf(
            DocumentationSearchHit hit,
            List<ExpectedPassage> expectedPassages
    ) {
        boolean expected = expectedPassages.stream()
                .anyMatch(expectedPassage ->
                        Objects.equals(
                                expectedPassage.sourceLocator(),
                                hit.sourceLocator()
                        )
                        && Objects.equals(
                                expectedPassage.chunkContentHash(),
                                hit.chunkContentHash()
                        )
                );

        return expected ? 1 : 0;
    }

    private Integer firstMatchingRank(
            List<Integer> rankedRelevances
    ) {
        for (int index = 0; index < rankedRelevances.size(); index++) {
            if (rankedRelevances.get(index) == 1) {
                return index + 1;
            }
        }

        return null;
    }

    private void requireEvaluableSource(UUID sourceId) {
        if (!sourceRepository.existsById(sourceId)) {
            throw new EntityNotFoundException(
                    "Source was not found: " + sourceId
            );
        }

        boolean hasSuccessfulRun = ingestionRunRepository.existsBySourceIdAndStatus(sourceId, IngestionRunStatus.SUCCEEDED);

        if (!hasSuccessfulRun) {
            throw new IllegalArgumentException(
                    "Source has no successful ingestion run: " + sourceId
            );
        }
    }

    private double ndcgAtTen(
            List<Integer> rankedRelevances,
            int expectedPassageCount
    ) {
        double dcg = 0;

        int returnedCount = Math.min(
                rankedRelevances.size(),
                RESULT_LIMIT
        );

        for (int i = 0; i < returnedCount; i++) {
            double discount = Math.log(i + 2) / Math.log(2);

            dcg += rankedRelevances.get(i) / discount;
        }

        double idealDcg = 0;
        int idealCount = Math.min(expectedPassageCount, RESULT_LIMIT);

        for (int index = 0; index < idealCount; index++) {
            double discount = Math.log(index + 2) / Math.log(2);

            idealDcg += 1.0 / discount;
        }

        return idealDcg == 0 ? 0 : dcg / idealDcg;
    }
}
