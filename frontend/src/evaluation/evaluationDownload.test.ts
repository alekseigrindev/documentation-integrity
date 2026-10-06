import { describe, expect, it } from 'vitest'
import { serializeEvaluationReport } from './evaluationDownload'
import type { EvaluationReport } from './evaluationApi'

describe('serializeEvaluationReport', () => {
  it('preserves the complete evaluation report as JSON', () => {
    const report: EvaluationReport = {
      sourceId: 'source-id',
      evaluationSetVersion: 2,
      retrievalMethod: 'HYBRID_RERANKED',
      retrievalConfiguration: {
        lexicalCandidateLimit: 50,
        vectorCandidateLimit: 50,
        rerankingCandidateLimit: 50,
        rerankingResultLimit: 10,
      },
      caseResults: [],
      summary: {
        totalCases: 0,
        casesFoundAtTen: 0,
        hitRateAtTen: 0,
        meanPrecisionAtTen: 0,
        meanRecallAtTen: 0,
        mrrAtTen: 0,
        meanNdcgAtTen: 0,
        p50LatencyMs: 0,
        p95LatencyMs: 0,
      },
    }

    expect(JSON.parse(serializeEvaluationReport(report))).toEqual(report)
  })
})
