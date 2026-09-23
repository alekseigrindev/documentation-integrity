import { describe, expect, it } from 'vitest'
import { serializeEvaluationReport } from './evaluationDownload'
import type { EvaluationReport } from './evaluationApi'

describe('serializeEvaluationReport', () => {
  it('preserves the complete evaluation report as JSON', () => {
    const report: EvaluationReport = {
      sourceId: 'source-id',
      evaluationSetVersion: 1,
      caseResults: [],
      summary: {
        totalCases: 0,
        casesFoundAtTen: 0,
        recallAtTen: 0,
        mrrAtTen: 0,
        p50LatencyMs: 0,
        p95LatencyMs: 0,
      },
    }

    expect(JSON.parse(serializeEvaluationReport(report, 'VECTOR'))).toEqual({
      retrievalMethod: 'VECTOR',
      ...report,
    })
  })
})
