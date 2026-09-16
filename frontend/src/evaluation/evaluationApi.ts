export type LexicalEvaluationCaseResult = {
  caseId: string
  query: string
  expectedSourceLocators: string[]
  firstMatchingRank: number | null
  durationMs: number
}

export type LexicalEvaluationSummary = {
  totalCases: number
  casesFoundAtTen: number
  recallAtTen: number
  mrrAtTen: number
  p50LatencyMs: number
  p95LatencyMs: number
}

export type LexicalEvaluationReport = {
  sourceId: string
  evaluationSetVersion: number
  caseResults: LexicalEvaluationCaseResult[]
  summary: LexicalEvaluationSummary
}

type ProblemDetail = {
  detail?: string
}

export class EvaluationApiError extends Error {
  readonly kind: 'http' | 'network'
  readonly status?: number

  constructor(
    message: string,
    kind: 'http' | 'network',
    status?: number,
  ) {
    super(message)
    this.name = 'EvaluationApiError'
    this.kind = kind
    this.status = status
  }
}

export async function runLexicalEvaluation(
  sourceId: string,
): Promise<LexicalEvaluationReport> {
  const response = await fetch('/api/admin/evaluations/lexical', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ sourceId }),
  }).catch(() => {
    throw new EvaluationApiError(
      'Unable to reach the Evaluation API',
      'network',
    )
  })

  if (response.status !== 200) {
    const problem = (await response.json().catch(() => null)) as
      | ProblemDetail
      | null

    throw new EvaluationApiError(
      problem?.detail ?? `Evaluation API returned status ${response.status}`,
      'http',
      response.status,
    )
  }

  return (await response.json()) as LexicalEvaluationReport
}
