package io.github.alekseigrindev.documentationintegrity.ingestion.evaluation;

import io.github.alekseigrindev.documentationintegrity.ingestion.run.IngestionRunRepository;
import io.github.alekseigrindev.documentationintegrity.ingestion.run.IngestionRunStatus;
import io.github.alekseigrindev.documentationintegrity.ingestion.search.DocumentationSearchHit;
import io.github.alekseigrindev.documentationintegrity.ingestion.search.DocumentationSearchService;
import io.github.alekseigrindev.documentationintegrity.ingestion.search.RetrievalMethod;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.SourceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LexicalEvaluationService {

    private static final int RESULT_LIMIT = 10;

    private final SourceRepository sourceRepository;
    private final IngestionRunRepository ingestionRunRepository;
    private final DocumentationSearchService documentationSearchService;
    private final EvaluationSetLoader evaluationSetLoader;

    public LexicalEvaluationReport evaluate(UUID sourceId, RetrievalMethod retrievalMethod) {
        requireEvaluableSource(sourceId);

        EvaluationSet evaluationSet = evaluationSetLoader.load();

        List<LexicalEvaluationCaseResult> caseResults = evaluationSet.cases().stream()
                .map(evaluationCase -> evaluateCase(
                        sourceId,
                        evaluationCase,
                        retrievalMethod
                ))
                .toList();

        return new LexicalEvaluationReport(
                sourceId,
                evaluationSet.evaluationSetVersion(),
                caseResults,
                summarize(caseResults)
        );

    }

    private LexicalEvaluationSummary summarize(List<LexicalEvaluationCaseResult> caseResults) {
        int totalCases = caseResults.size();

        if (totalCases == 0) {
            return new LexicalEvaluationSummary(0, 0, 0, 0 , 0, 0);
        }

        int casesFoundAtTen = Math.toIntExact(
                caseResults.stream()
                        .filter(result -> result.firstMatchingRank() != null)
                        .count()
                );

        double recallAtTen = (double) casesFoundAtTen / totalCases;

        double mrrAtTen = caseResults.stream()
                .mapToDouble(result ->
                        result.firstMatchingRank() == null
                                ? 0
                                : 1.0 / result.firstMatchingRank())
                .average()
                .orElse(0);

        List<Long> sortedDurations = caseResults.stream()
                .map(LexicalEvaluationCaseResult::durationMs)
                .sorted()
                .collect(Collectors.toList());

        return new LexicalEvaluationSummary(
                totalCases,
                casesFoundAtTen,
                recallAtTen,
                mrrAtTen,
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
        long startedAt = System.nanoTime();

        List<DocumentationSearchHit> searchHits = documentationSearchService.search(
                evaluationCase.query(),
                Set.of(sourceId),
                retrievalMethod);

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);

        return new LexicalEvaluationCaseResult(
                evaluationCase.id(),
                evaluationCase.query(),
                evaluationCase.expectedSourceLocators(),
                firstMatchingRank(
                        searchHits,
                        evaluationCase.expectedSourceLocators()
                ),
                durationMs
        );


    }

    private Integer firstMatchingRank(
            List<DocumentationSearchHit> searchHits,
            List<String> expectedSourceLocators
    ) {
        int resultCount = Math.min(searchHits.size(), RESULT_LIMIT);

        for (int i = 0; i < resultCount; i++) {
            if (expectedSourceLocators.contains(
                    searchHits.get(i).sourceLocator()
            )) {
                return i + 1;
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
}
