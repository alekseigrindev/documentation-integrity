import { fireEvent, render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { listSources } from '../sources/sourceApi'
import { listRetrievalMethods } from '../search/searchApi'
import EvaluationManagement from './EvaluationManagement'
import { runEvaluation } from './evaluationApi'
import { downloadEvaluationReport } from './evaluationDownload'

vi.mock('../sources/sourceApi', () => ({
  listSources: vi.fn(),
}))

vi.mock('../search/searchApi', () => ({
  listRetrievalMethods: vi.fn(),
}))

vi.mock('./evaluationApi', () => ({
  runEvaluation: vi.fn(),
}))

vi.mock('./evaluationDownload', () => ({
  downloadEvaluationReport: vi.fn(),
}))

const source = {
  id: 'f59642e6-627f-434e-aa34-eb36b387ad46',
  publisherId: '53f81218-3017-4852-a65f-e68e79813436',
  connectorType: 'local-directory',
  sourceKey: 'github-docs',
  name: 'GitHub Docs',
}

const report = {
  sourceId: source.id,
  evaluationSetVersion: 1,
  caseResults: [
    {
      caseId: 'workflow-permissions-syntax',
      query: 'Where do I define workflow permissions?',
      expectedSourceLocators: [
        'content/actions/reference/workflows-and-actions/workflow-syntax.md',
      ],
      firstMatchingRank: 2,
      durationMs: 14,
    },
    {
      caseId: 'manual-workflow-run',
      query: 'How can I run a workflow manually?',
      expectedSourceLocators: [
        'content/actions/how-tos/manage-workflow-runs/manually-run-a-workflow.md',
      ],
      firstMatchingRank: null,
      durationMs: 18,
    },
  ],
  summary: {
    totalCases: 2,
    casesFoundAtTen: 1,
    recallAtTen: 0.5,
    mrrAtTen: 0.25,
    p50LatencyMs: 14,
    p95LatencyMs: 18,
  },
}

describe('EvaluationManagement', () => {
  beforeEach(() => {
    vi.mocked(listSources).mockReset()
    vi.mocked(listRetrievalMethods).mockReset()
    vi.mocked(runEvaluation).mockReset()
    vi.mocked(downloadEvaluationReport).mockReset()
    vi.mocked(listSources).mockResolvedValue([source])
    vi.mocked(listRetrievalMethods).mockResolvedValue([
      { retrievalMethod: 'LEXICAL', displayName: 'Lexical search' },
      { retrievalMethod: 'VECTOR', displayName: 'Vector search' },
    ])
  })

  it('runs and displays a source-scoped lexical evaluation', async () => {
    vi.mocked(runEvaluation).mockResolvedValue(report)

    render(<EvaluationManagement />)

    fireEvent.change(await screen.findByLabelText('Source'), {
      target: { value: source.id },
    })
    await screen.findByRole('option', { name: 'Lexical search' })
    fireEvent.click(
      screen.getByRole('button', { name: 'Run evaluation' }),
    )

    expect(await screen.findByText('Lexical search evaluation report')).toBeInTheDocument()
    expect(runEvaluation).toHaveBeenCalledWith(source.id, 'LEXICAL')
    expect(screen.getByText('1 / 2')).toBeInTheDocument()
    expect(screen.getByText('50.0%')).toBeInTheDocument()
    expect(screen.getByText('0.250')).toBeInTheDocument()
    expect(screen.getByText('Found at rank 2')).toBeInTheDocument()
    expect(screen.getByText('Not found in top 10')).toBeInTheDocument()

    fireEvent.click(
      screen.getByRole('button', { name: 'Save evaluation results' }),
    )

    expect(downloadEvaluationReport).toHaveBeenCalledWith(
      report,
      source.sourceKey,
      'LEXICAL',
    )
  })

  it('runs vector evaluation and labels its report correctly', async () => {
    vi.mocked(runEvaluation).mockResolvedValue(report)

    render(<EvaluationManagement />)

    fireEvent.change(await screen.findByLabelText('Source'), {
      target: { value: source.id },
    })
    fireEvent.change(await screen.findByLabelText('Search method'), {
      target: { value: 'VECTOR' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Run evaluation' }))

    expect(await screen.findByText('Vector search evaluation report')).toBeInTheDocument()
    expect(runEvaluation).toHaveBeenCalledWith(source.id, 'VECTOR')

    fireEvent.click(screen.getByRole('button', { name: 'Save evaluation results' }))
    expect(downloadEvaluationReport).toHaveBeenCalledWith(
      report,
      source.sourceKey,
      'VECTOR',
    )
  })

  it('shows a safe evaluation failure', async () => {
    vi.mocked(runEvaluation).mockRejectedValue(
      new Error('Source has no successful ingestion run'),
    )

    render(<EvaluationManagement />)

    fireEvent.change(await screen.findByLabelText('Source'), {
      target: { value: source.id },
    })
    await screen.findByRole('option', { name: 'Lexical search' })
    fireEvent.click(
      screen.getByRole('button', { name: 'Run evaluation' }),
    )

    expect(
      await screen.findByRole('alert'),
    ).toHaveTextContent('Source has no successful ingestion run')
  })
})
