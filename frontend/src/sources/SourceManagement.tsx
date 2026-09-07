import { type FormEvent, useEffect, useState } from 'react'
import { listPublishers, type Publisher } from '../publishers/publisherApi'
import { listConnectors, type Connector } from './connectorApi'
import {
  latestIngestionRun,
  synchronizeSource,
  type IngestionRun,
} from './ingestionRunApi'
import {
  chooseLocalDirectory,
  createSource,
  listSources,
  type CreateSourceRequest,
  type Source,
  updateSource,
} from './sourceApi'

const localDirectoryConnectorType = 'local-directory'

type SourceField =
  | 'publisherId'
  | 'connectorType'
  | 'sourceKey'
  | 'name'
  | 'localDirectoryPath'
type ValidationErrors = Partial<Record<SourceField, string>>
type SourceSyncResult = {
  kind: 'succeeded' | 'failed'
  message: string
}

function orderSources(sources: Source[]) {
  return [...sources].sort((left, right) => {
    const nameOrder = left.name.localeCompare(right.name)
    return nameOrder !== 0 ? nameOrder : left.id.localeCompare(right.id)
  })
}

function localDirectoryPathFrom(sourceUrl?: string) {
  if (!sourceUrl) {
    return ''
  }

  try {
    const url = new URL(sourceUrl)
    return url.protocol === 'file:'
      ? decodeURIComponent(url.pathname)
      : sourceUrl
  } catch {
    return sourceUrl
  }
}

function sourceUrlFromLocalDirectoryPath(path: string) {
  const trimmedPath = path.trim()
  return trimmedPath.startsWith('file:')
    ? trimmedPath
    : new URL(`file://${trimmedPath}`).toString()
}

function syncResultFromRun(run: IngestionRun): SourceSyncResult {
  if (run.status === 'SUCCEEDED') {
    return { kind: 'succeeded', message: 'Succeeded' }
  }

  return {
    kind: 'failed',
    message: run.failureMessage ?? 'Synchronization failed.',
  }
}

function SourceManagement() {
  const [publishers, setPublishers] = useState<Publisher[] | null>(null)
  const [sources, setSources] = useState<Source[] | null>(null)
  const [connectors, setConnectors] = useState<Connector[] | null>(null)
  const [loadFailed, setLoadFailed] = useState(false)
  const [createOpen, setCreateOpen] = useState(false)
  const [publisherId, setPublisherId] = useState('')
  const [connectorType, setConnectorType] = useState('')
  const [sourceKey, setSourceKey] = useState('')
  const [sourceName, setSourceName] = useState('')
  const [localDirectoryPath, setLocalDirectoryPath] = useState('')
  const [validationErrors, setValidationErrors] = useState<ValidationErrors>({})
  const [createFailed, setCreateFailed] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [editingSource, setEditingSource] = useState<Source | null>(null)
  const [pickingDirectory, setPickingDirectory] = useState(false)
  const [directoryPickerFailed, setDirectoryPickerFailed] = useState(false)
  const [syncingSourceId, setSyncingSourceId] = useState<string | null>(null)
  const [syncResults, setSyncResults] = useState<
    Record<string, SourceSyncResult>
  >({})

  useEffect(() => {
    Promise.all([listPublishers(), listSources(), listConnectors()])
      .then(([loadedPublishers, loadedSources, loadedConnectors]) => {
        setPublishers(loadedPublishers)
        setSources(loadedSources)
        setConnectors(loadedConnectors)
      })
      .catch(() => setLoadFailed(true))
  }, [])

  function resetCreateForm() {
    setPublisherId('')
    setConnectorType('')
    setSourceKey('')
    setSourceName('')
    setLocalDirectoryPath('')
    setValidationErrors({})
    setCreateFailed(false)
    setEditingSource(null)
    setDirectoryPickerFailed(false)
  }

  function openCreateModal() {
    resetCreateForm()
    setConnectorType(connectors?.[0]?.type ?? '')
    setCreateOpen(true)
  }

  function openEditModal(source: Source) {
    setPublisherId(source.publisherId)
    setConnectorType(source.connectorType)
    setSourceKey(source.sourceKey)
    setSourceName(source.name)
    setLocalDirectoryPath(localDirectoryPathFrom(source.sourceUrl))
    setValidationErrors({})
    setCreateFailed(false)
    setEditingSource(source)
    setCreateOpen(true)
  }

  function closeCreateModal() {
    if (submitting) {
      return
    }

    setCreateOpen(false)
    resetCreateForm()
  }

  function clearFieldError(field: SourceField) {
    setValidationErrors((current) => {
      const remaining = { ...current }
      delete remaining[field]
      return remaining
    })
    setCreateFailed(false)
  }

  async function chooseDirectory() {
    setDirectoryPickerFailed(false)
    setPickingDirectory(true)

    try {
      const sourceUrl = await chooseLocalDirectory()
      if (sourceUrl) {
        setLocalDirectoryPath(localDirectoryPathFrom(sourceUrl))
        clearFieldError('localDirectoryPath')
      }
    } catch {
      setDirectoryPickerFailed(true)
    } finally {
      setPickingDirectory(false)
    }
  }

  async function syncSource(sourceId: string) {
    setSyncResults((current) => {
      const updated = { ...current }
      delete updated[sourceId]
      return updated
    })
    setSyncingSourceId(sourceId)

    try {
      const run = await synchronizeSource(sourceId)
      setSyncResults((current) => ({
        ...current,
        [sourceId]: syncResultFromRun(run),
      }))
    } catch {
      try {
        const latestRun = await latestIngestionRun(sourceId)
        setSyncResults((current) => ({
          ...current,
          [sourceId]:
            latestRun?.status === 'FAILED'
              ? syncResultFromRun(latestRun)
              : {
                  kind: 'failed',
                  message: 'Synchronization failed. Try again.',
                },
        }))
      } catch {
        setSyncResults((current) => ({
          ...current,
          [sourceId]: {
            kind: 'failed',
            message: 'Synchronization failed. Try again.',
          },
        }))
      }
    } finally {
      setSyncingSourceId(null)
    }
  }

  async function handleSave(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const request: CreateSourceRequest = {
      publisherId,
      connectorType,
      sourceKey: sourceKey.trim(),
      name: sourceName.trim(),
      sourceUrl:
        connectorType === localDirectoryConnectorType && localDirectoryPath.trim()
          ? sourceUrlFromLocalDirectoryPath(localDirectoryPath)
          : undefined,
    }
    const errors: ValidationErrors = {}

    if (!request.publisherId) {
      errors.publisherId = 'Select a Publisher.'
    }
    if (!request.connectorType) {
      errors.connectorType = 'Select a Connector.'
    }
    if (!request.sourceKey) {
      errors.sourceKey = 'Source key is required.'
    }
    if (!request.name) {
      errors.name = 'Source name is required.'
    }
    if (
      request.connectorType === localDirectoryConnectorType &&
      !localDirectoryPath.trim()
    ) {
      errors.localDirectoryPath = 'Local directory path is required.'
    }

    if (Object.keys(errors).length > 0) {
      setValidationErrors(errors)
      return
    }

    setValidationErrors({})
    setCreateFailed(false)
    setSubmitting(true)

    try {
      const source = editingSource
        ? await updateSource(editingSource.id, request)
        : await createSource(request)
      setSources((current) =>
        orderSources([
          ...(current ?? []).filter((item) => item.id !== source.id),
          source,
        ]),
      )
      setCreateOpen(false)
      resetCreateForm()
    } catch {
      setCreateFailed(true)
    } finally {
      setSubmitting(false)
    }
  }

  const publishersById = new Map(
    (publishers ?? []).map((publisher) => [publisher.id, publisher.name]),
  )
  const canCreate =
    !loadFailed && publishers !== null && sources !== null && connectors !== null
  const isEditing = editingSource !== null

  return (
    <section
      id="sources"
      className="source-management"
      aria-labelledby="sources-title"
    >
      <div className="section-heading">
        <div>
          <h2 id="sources-title">Sources</h2>
          <p className="section-description">
            Approved documentation locations associated with a Publisher.
          </p>
        </div>
        {canCreate && publishers !== null && publishers.length > 0 ? (
          <button
            className="primary-button"
            type="button"
            onClick={openCreateModal}
          >
            Create Source
          </button>
        ) : null}
      </div>

      <div className="content-panel">
        {loadFailed ? (
          <p className="request-error" role="alert">
            Unable to load Source management.
          </p>
        ) : publishers === null || sources === null ? (
          <p role="status">Loading Sources…</p>
        ) : publishers.length === 0 ? (
          <p className="empty-state">
            Create a Publisher before adding a Source.
          </p>
        ) : sources.length === 0 ? (
          <p className="empty-state">No Sources are registered yet.</p>
        ) : (
          <ul className="source-list" aria-label="Sources">
            {sources.map((source) => (
              <li key={source.id}>
                <div>
                  <strong>{source.name}</strong>
                  <p className="source-details">
                    Publisher:{' '}
                    {publishersById.get(source.publisherId) ??
                      'Unknown Publisher'}
                    <span aria-hidden="true"> · </span>
                    Key: {source.sourceKey}
                  </p>
                </div>
                <div className="source-actions">
                  <div className="source-action-buttons">
                    <span className="connector-badge">
                      {source.connectorType}
                    </span>
                    <button
                      className="primary-button"
                      type="button"
                      disabled={syncingSourceId === source.id}
                      onClick={() => syncSource(source.id)}
                    >
                      {syncingSourceId === source.id ? 'Syncing…' : 'Sync'}
                    </button>
                    <button
                      className="secondary-button"
                      type="button"
                      disabled={syncingSourceId === source.id}
                      onClick={() => openEditModal(source)}
                    >
                      Edit
                    </button>
                  </div>
                  {syncResults[source.id] ? (
                    <p
                      className={`source-sync-result source-sync-result-${syncResults[source.id].kind}`}
                      role={
                        syncResults[source.id].kind === 'failed'
                          ? 'alert'
                          : 'status'
                      }
                    >
                      {syncResults[source.id].message}
                    </p>
                  ) : null}
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>

      {createOpen ? (
        <div className="modal-backdrop">
          <div
            className="modal-dialog"
            role="dialog"
            aria-modal="true"
            aria-labelledby="source-dialog-title"
            onKeyDown={(event) => {
              if (event.key === 'Escape') {
                closeCreateModal()
              }
            }}
          >
            <div className="modal-header">
              <h3 id="source-dialog-title">
                {isEditing ? 'Edit Source' : 'Create Source'}
              </h3>
              <button
                className="icon-button"
                type="button"
                aria-label="Close Source dialog"
                disabled={submitting}
                onClick={closeCreateModal}
              >
                ×
              </button>
            </div>

            <form className="source-form" onSubmit={handleSave}>
              <label htmlFor="source-name">Source name</label>
              <input
                id="source-name"
                name="sourceName"
                value={sourceName}
                disabled={submitting}
                autoFocus
                aria-invalid={validationErrors.name !== undefined}
                aria-describedby={
                  validationErrors.name ? 'source-name-error' : undefined
                }
                onChange={(event) => {
                  setSourceName(event.target.value)
                  clearFieldError('name')
                }}
              />
              {validationErrors.name ? (
                <p id="source-name-error" className="field-error">
                  {validationErrors.name}
                </p>
              ) : null}

              <label htmlFor="source-publisher">Publisher</label>
              <select
                id="source-publisher"
                name="publisherId"
                value={publisherId}
                disabled={submitting}
                aria-invalid={validationErrors.publisherId !== undefined}
                aria-describedby={
                  validationErrors.publisherId
                    ? 'source-publisher-error'
                    : undefined
                }
                onChange={(event) => {
                  setPublisherId(event.target.value)
                  clearFieldError('publisherId')
                }}
              >
                <option value="">Select a Publisher</option>
                {publishers?.map((publisher) => (
                  <option key={publisher.id} value={publisher.id}>
                    {publisher.name}
                  </option>
                ))}
              </select>
              {validationErrors.publisherId ? (
                <p id="source-publisher-error" className="field-error">
                  {validationErrors.publisherId}
                </p>
              ) : null}

              <label htmlFor="source-connector">Connector</label>
              <select
                id="source-connector"
                name="connectorType"
                value={connectorType}
                disabled={submitting}
                aria-invalid={validationErrors.connectorType !== undefined}
                aria-describedby={
                  validationErrors.connectorType
                    ? 'source-connector-error'
                    : undefined
                }
                onChange={(event) => {
                  setConnectorType(event.target.value)
                  clearFieldError('connectorType')
                }}
              >
                <option value="">Select a Connector</option>
                {connectors?.map((connector) => (
                  <option key={connector.type} value={connector.type}>
                    {connector.type}
                  </option>
                ))}
              </select>
              {validationErrors.connectorType ? (
                <p id="source-connector-error" className="field-error">
                  {validationErrors.connectorType}
                </p>
              ) : null}

              {connectorType === localDirectoryConnectorType ? (
                <>
                  <label htmlFor="source-local-directory">
                    Local directory path
                  </label>
                  <input
                    id="source-local-directory"
                    name="localDirectoryPath"
                    type="text"
                    value={localDirectoryPath}
                    placeholder="/Users/you/Documents/docs"
                    disabled={submitting}
                    aria-invalid={
                      validationErrors.localDirectoryPath !== undefined
                    }
                    aria-describedby={
                      validationErrors.localDirectoryPath
                        ? 'source-local-directory-error'
                        : undefined
                    }
                    onChange={(event) => {
                      setLocalDirectoryPath(event.target.value)
                      clearFieldError('localDirectoryPath')
                    }}
                  />
                  <button
                    className="secondary-button"
                    type="button"
                    disabled={submitting || pickingDirectory}
                    onClick={chooseDirectory}
                  >
                    {pickingDirectory ? 'Choosing…' : 'Choose directory'}
                  </button>
                  {validationErrors.localDirectoryPath ? (
                    <p
                      id="source-local-directory-error"
                      className="field-error"
                    >
                      {validationErrors.localDirectoryPath}
                    </p>
                  ) : null}
                  {directoryPickerFailed ? (
                    <p className="request-error" role="alert">
                      Unable to open the local directory picker. Enter the
                      path manually.
                    </p>
                  ) : null}
                </>
              ) : null}

              <label htmlFor="source-key">Source key</label>
              <input
                id="source-key"
                name="sourceKey"
                value={sourceKey}
                disabled={submitting}
                aria-invalid={validationErrors.sourceKey !== undefined}
                aria-describedby={
                  validationErrors.sourceKey ? 'source-key-error' : undefined
                }
                onChange={(event) => {
                  setSourceKey(event.target.value)
                  clearFieldError('sourceKey')
                }}
              />
              {validationErrors.sourceKey ? (
                <p id="source-key-error" className="field-error">
                  {validationErrors.sourceKey}
                </p>
              ) : null}

              {createFailed ? (
                <p className="request-error" role="alert">
                  {isEditing
                    ? 'Unable to update Source. Try again.'
                    : 'Unable to create Source. Try again.'}
                </p>
              ) : null}

              <div className="modal-actions">
                <button
                  className="secondary-button"
                  type="button"
                  disabled={submitting}
                  onClick={closeCreateModal}
                >
                  Cancel
                </button>
                <button
                  className="primary-button"
                  type="submit"
                  disabled={submitting}
                >
                  {submitting
                    ? isEditing
                      ? 'Saving…'
                      : 'Creating…'
                    : isEditing
                      ? 'Save Source'
                      : 'Create Source'}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </section>
  )
}

export default SourceManagement
