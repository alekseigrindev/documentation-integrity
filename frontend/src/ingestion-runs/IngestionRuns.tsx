import { useEffect, useState } from 'react'
import { listSources, type Source } from '../sources/sourceApi'
import { listIngestionRuns, type IngestionRun } from './ingestionRunApi'

function formattedTime(value: string | null) {
  return value ? new Date(value).toLocaleString() : 'Not finished'
}

function IngestionRuns() {
  const [runs, setRuns] = useState<IngestionRun[] | null>(null)
  const [sources, setSources] = useState<Source[] | null>(null)
  const [loadFailed, setLoadFailed] = useState(false)
  const [refreshing, setRefreshing] = useState(false)

  useEffect(() => {
    Promise.all([listIngestionRuns(), listSources()])
      .then(([loadedRuns, loadedSources]) => {
        setRuns(loadedRuns)
        setSources(loadedSources)
      })
      .catch(() => setLoadFailed(true))
  }, [])

  async function refreshRuns() {
    setRefreshing(true)

    try {
      const [loadedRuns, loadedSources] = await Promise.all([
        listIngestionRuns(),
        listSources(),
      ])
      setLoadFailed(false)
      setRuns(loadedRuns)
      setSources(loadedSources)
    } catch {
      setLoadFailed(true)
    } finally {
      setRefreshing(false)
    }
  }

  const sourceNames = new Map(
    (sources ?? []).map((source) => [source.id, source.name]),
  )

  return (
    <section
      id="ingestion-runs"
      className="ingestion-run-management"
      aria-labelledby="ingestion-runs-title"
    >
      <div className="section-heading">
        <div>
          <h2 id="ingestion-runs-title">Ingestion runs</h2>
          <p className="section-description">
            Recent synchronization attempts across registered Sources.
          </p>
        </div>
        <button
          className="secondary-button"
          type="button"
          disabled={refreshing}
          onClick={refreshRuns}
        >
          Refresh
        </button>
      </div>

      <div className="content-panel">
        {loadFailed ? (
          <p className="request-error" role="alert">
            Unable to load Ingestion runs.
          </p>
        ) : runs === null || sources === null ? (
          <p role="status">Loading Ingestion runs…</p>
        ) : runs.length === 0 ? (
          <p className="empty-state">No ingestion runs have been recorded.</p>
        ) : (
          <ul className="ingestion-run-list" aria-label="Ingestion runs">
            {runs.map((run) => (
              <li key={run.id}>
                <div>
                  <strong>
                    {sourceNames.get(run.sourceId) ?? 'Unknown Source'}
                  </strong>
                  <p className="ingestion-run-details">
                    Started: {formattedTime(run.startedAt)}
                    <span aria-hidden="true"> · </span>
                    Finished: {formattedTime(run.finishedAt)}
                  </p>
                </div>
                <div className="ingestion-run-outcome">
                  <span
                    className={`ingestion-run-status ingestion-run-status-${run.status.toLowerCase()}`}
                  >
                    {run.status}
                  </span>
                  {run.status === 'SUCCEEDED' ? (
                    <p className="ingestion-run-changes">
                      Documents: added {run.documentsAdded ?? 0}
                      <span aria-hidden="true"> · </span>
                      updated {run.documentsUpdated ?? 0}
                      <span aria-hidden="true"> · </span>
                      removed {run.documentsRemoved ?? 0}
                    </p>
                  ) : null}
                  {run.status === 'FAILED' && run.failureMessage ? (
                    <p className="ingestion-run-failure" role="alert">
                      {run.failureMessage}
                    </p>
                  ) : null}
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </section>
  )
}

export default IngestionRuns
