package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.evaluation;

import io.github.alekseigrindev.documentationintegrity.ingestion.evaluation.LexicalEvaluationReport;
import io.github.alekseigrindev.documentationintegrity.ingestion.evaluation.LexicalEvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/admin/evaluations")
@RequiredArgsConstructor
public class EvaluationAdminController {

    private final LexicalEvaluationService evaluationService;

    @PostMapping("/lexical")
    public LexicalEvaluationReport evaluateLexical(
            @Valid @RequestBody LexicalEvaluationRequest request
    ) {
        return evaluationService.evaluate(request.sourceId());
    }

}
