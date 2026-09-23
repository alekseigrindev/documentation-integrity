import { afterEach, describe, expect, it, vi } from 'vitest'
import { runEvaluation } from './evaluationApi'

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('runEvaluation', () => {
  it('sends the selected retrieval method and Source to the backend', async () => {
    const report = {
      sourceId: 'source-a',
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
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify(report), { status: 200 }),
    )
    vi.stubGlobal('fetch', fetchMock)

    await expect(runEvaluation('source-a', 'VECTOR')).resolves.toEqual(report)
    expect(fetchMock).toHaveBeenCalledWith(
      '/api/admin/evaluations/lexical?method=VECTOR',
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ sourceId: 'source-a' }),
      },
    )
  })
})
