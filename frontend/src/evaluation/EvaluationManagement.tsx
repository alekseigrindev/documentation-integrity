import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { listSources, type Source } from '../sources/sourceApi'
import {
  listRetrievalMethods,
  type RetrievalMethod,
  type RetrievalMethodOption,
} from '../search/searchApi'
import {
  runEvaluation,
  type EvaluationReport,
} from './evaluationApi'
import { downloadEvaluationReport } from './evaluationDownload'

function percentage(value: number) {
  return `${(value * 100).toFixed(1)}%`
}

function EvaluationManagement() {
  const [sources, setSources] = useState<Source[] | null>(null)
  const [selectedSourceId, setSelectedSourceId] = useState('')
  const [retrievalMethods, setRetrievalMethods] =
    useState<RetrievalMethodOption[] | null>(null)
  const [retrievalMethod, setRetrievalMethod] = useState<RetrievalMethod | ''>('')
  const [result, setResult] = useState<{
    report: EvaluationReport
    retrievalMethod: RetrievalMethod
    displayName: string
  } | null>(null)
  const [sourceLoadFailed, setSourceLoadFailed] = useState(false)
  const [retrievalMethodsFailed, setRetrievalMethodsFailed] = useState(false)
  const [evaluationError, setEvaluationError] = useState<string | null>(null)
  const [running, setRunning] = useState(false)

  useEffect(() => {
    listSources()
      .then((loadedSources) => {
        setSources(loadedSources)
        setSourceLoadFailed(false)
      })
      .catch(() => {
        setSources([])
        setSourceLoadFailed(true)
      })
  }, [])

  useEffect(() => {
    listRetrievalMethods()
      .then((methods) => {
        setRetrievalMethods(methods)
        setRetrievalMethod(methods[0]?.retrievalMethod ?? '')
      })
      .catch(() => {
        setRetrievalMethodsFailed(true)
        setRetrievalMethods([])
      })
  }, [])

  async function submitEvaluation(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!selectedSourceId || !retrievalMethod) {
      return
    }

    setRunning(true)
    setEvaluationError(null)
    setResult(null)

    try {
      const report = await runEvaluation(selectedSourceId, retrievalMethod)
      setResult({
        report,
        retrievalMethod,
        displayName: retrievalMethods?.find(
          (method) => method.retrievalMethod === retrievalMethod,
        )?.displayName ?? retrievalMethod,
      })
    } catch (error) {
      setEvaluationError(
        error instanceof Error
          ? error.message
          : 'Unable to run evaluation.',
      )
    } finally {
      setRunning(false)
    }
  }

  const evaluatedSource = sources?.find(
    (source) => source.id === result?.report.sourceId,
  )
  const report = result?.report

  return (
    <section
      id="evaluations"
      className="evaluation-management"
      aria-labelledby="evaluations-title"
    >
      <div className="section-heading">
        <div>
          <h2 id="evaluations-title">Evaluations</h2>
          <p className="section-description">
            Measure a search method against the fixed evaluation set for one
            synchronized Source.
          </p>
        </div>
      </div>

      <form className="evaluation-form" onSubmit={submitEvaluation}>
        <label htmlFor="evaluation-source">Source</label>
        <div className="evaluation-controls">
          <select
            id="evaluation-source"
            value={selectedSourceId}
            disabled={running || sources === null || sourceLoadFailed}
            onChange={(event) => setSelectedSourceId(event.target.value)}
          >
            <option value="">Select a Source</option>
            {(sources ?? []).map((source) => (
              <option key={source.id} value={source.id}>
                {source.name}
              </option>
            ))}
          </select>
          <button
            className="primary-button"
            type="submit"
            disabled={!selectedSourceId || !retrievalMethod || running}
          >
            Run evaluation
          </button>
        </div>
        <div className="retrieval-method-selector">
          <label htmlFor="evaluation-retrieval-method">Search method</label>
          <select
            id="evaluation-retrieval-method"
            value={retrievalMethod}
            disabled={running || retrievalMethods === null || retrievalMethodsFailed}
            onChange={(event) =>
              setRetrievalMethod(event.target.value as RetrievalMethod)
            }
          >
            {retrievalMethods === null ? (
              <option value="">Loading methods…</option>
            ) : retrievalMethods.length === 0 ? (
              <option value="">No search methods available</option>
            ) : (
              retrievalMethods.map((method) => (
                <option
                  key={method.retrievalMethod}
                  value={method.retrievalMethod}
                >
                  {method.displayName}
                </option>
              ))
            )}
          </select>
        </div>
        {retrievalMethodsFailed ? (
          <p className="request-error" role="alert">
            Unable to load search methods. Try reloading the page.
          </p>
        ) : null}
      </form>

      <div className="content-panel">
        {sourceLoadFailed ? (
          <p className="request-error" role="alert">
            Unable to load Sources.
          </p>
        ) : sources === null ? (
          <p role="status">Loading Sources…</p>
        ) : sources.length === 0 ? (
          <p className="empty-state">
            Register and synchronize a Source before running an evaluation.
          </p>
        ) : running ? (
          <p role="status">Running evaluation…</p>
        ) : evaluationError ? (
          <p className="request-error" role="alert">
            {evaluationError}
          </p>
        ) : report ? (
          <div className="evaluation-report">
            <div className="evaluation-report-heading">
              <div>
                <h3>{result.displayName} evaluation report</h3>
                <p>
                  {evaluatedSource?.name ?? 'Unknown Source'} · Evaluation set
                  v{report.evaluationSetVersion}
                </p>
              </div>
              <button
                className="secondary-button"
                type="button"
                onClick={() =>
                  downloadEvaluationReport(
                    report,
                    evaluatedSource?.sourceKey ?? report.sourceId,
                    result.retrievalMethod,
                  )
                }
              >
                Save evaluation results
              </button>
            </div>

            <dl className="evaluation-summary">
              <div>
                <dt>Cases found @10</dt>
                <dd>
                  {report.summary.casesFoundAtTen} / {report.summary.totalCases}
                </dd>
              </div>
              <div>
                <dt>Recall@10</dt>
                <dd>{percentage(report.summary.recallAtTen)}</dd>
              </div>
              <div>
                <dt>MRR@10</dt>
                <dd>{report.summary.mrrAtTen.toFixed(3)}</dd>
              </div>
              <div>
                <dt>p50 latency</dt>
                <dd>{report.summary.p50LatencyMs} ms</dd>
              </div>
              <div>
                <dt>p95 latency</dt>
                <dd>{report.summary.p95LatencyMs} ms</dd>
              </div>
            </dl>

            <ol className="evaluation-case-list" aria-label="Evaluation cases">
              {report.caseResults.map((caseResult) => (
                <li key={caseResult.caseId}>
                  <div className="evaluation-case-heading">
                    <strong>{caseResult.query}</strong>
                    <span
                      className={
                        caseResult.firstMatchingRank === null
                          ? 'evaluation-result evaluation-result-missed'
                          : 'evaluation-result evaluation-result-found'
                      }
                    >
                      {caseResult.firstMatchingRank === null
                        ? 'Not found in top 10'
                        : `Found at rank ${caseResult.firstMatchingRank}`}
                    </span>
                  </div>
                  <p className="evaluation-case-id">{caseResult.caseId}</p>
                  <p className="evaluation-case-locator">
                    Expected:{' '}
                    {caseResult.expectedSourceLocators.join(', ')}
                  </p>
                  <p className="evaluation-case-duration">
                    Duration: {caseResult.durationMs} ms
                  </p>
                </li>
              ))}
            </ol>
          </div>
        ) : (
          <p className="empty-state">
            Select one synchronized Source and run its evaluation.
          </p>
        )}
      </div>
    </section>
  )
}

export default EvaluationManagement
