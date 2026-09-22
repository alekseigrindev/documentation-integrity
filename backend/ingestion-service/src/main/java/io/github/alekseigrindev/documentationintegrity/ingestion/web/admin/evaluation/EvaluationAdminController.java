package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.evaluation;

import io.github.alekseigrindev.documentationintegrity.ingestion.evaluation.LexicalEvaluationReport;
import io.github.alekseigrindev.documentationintegrity.ingestion.evaluation.LexicalEvaluationService;
import io.github.alekseigrindev.documentationintegrity.ingestion.search.RetrievalMethod;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/admin/evaluations")
@RequiredArgsConstructor
public class EvaluationAdminController {

    private final LexicalEvaluationService evaluationService;

    @PostMapping("/lexical")
    public LexicalEvaluationReport evaluateLexical(
            @Valid @RequestBody LexicalEvaluationRequest request,
            @RequestParam(name = "method", defaultValue = "LEXICAL") RetrievalMethod retrievalMethod
    ) {
        return evaluationService.evaluate(
                request.sourceId(),
                retrievalMethod);
    }

}
