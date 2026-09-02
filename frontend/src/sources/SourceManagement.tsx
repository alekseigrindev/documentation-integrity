import { type FormEvent, useEffect, useState } from 'react'
import { listPublishers, type Publisher } from '../publishers/publisherApi'
import {
  createSource,
  listSources,
  type CreateSourceRequest,
  type Source,
} from './sourceApi'

type SourceField = 'publisherId' | 'sourceKey' | 'name'
type ValidationErrors = Partial<Record<SourceField, string>>

function orderSources(sources: Source[]) {
  return [...sources].sort((left, right) => {
    const nameOrder = left.name.localeCompare(right.name)
    return nameOrder !== 0 ? nameOrder : left.id.localeCompare(right.id)
  })
}

function SourceManagement() {
  const [publishers, setPublishers] = useState<Publisher[] | null>(null)
  const [sources, setSources] = useState<Source[] | null>(null)
  const [loadFailed, setLoadFailed] = useState(false)
  const [createOpen, setCreateOpen] = useState(false)
  const [publisherId, setPublisherId] = useState('')
  const [sourceKey, setSourceKey] = useState('')
  const [sourceName, setSourceName] = useState('')
  const [validationErrors, setValidationErrors] = useState<ValidationErrors>({})
  const [createFailed, setCreateFailed] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    Promise.all([listPublishers(), listSources()])
      .then(([loadedPublishers, loadedSources]) => {
        setPublishers(loadedPublishers)
        setSources(loadedSources)
      })
      .catch(() => setLoadFailed(true))
  }, [])

  function resetCreateForm() {
    setPublisherId('')
    setSourceKey('')
    setSourceName('')
    setValidationErrors({})
    setCreateFailed(false)
  }

  function openCreateModal() {
    resetCreateForm()
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

  async function handleCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const request: CreateSourceRequest = {
      publisherId,
      connectorType: 'github',
      sourceKey: sourceKey.trim(),
      name: sourceName.trim(),
    }
    const errors: ValidationErrors = {}

    if (!request.publisherId) {
      errors.publisherId = 'Select a Publisher.'
    }
    if (!request.sourceKey) {
      errors.sourceKey = 'Source key is required.'
    }
    if (!request.name) {
      errors.name = 'Source name is required.'
    }

    if (Object.keys(errors).length > 0) {
      setValidationErrors(errors)
      return
    }

    setValidationErrors({})
    setCreateFailed(false)
    setSubmitting(true)

    try {
      const source = await createSource(request)
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
  const canCreate = !loadFailed && publishers !== null && sources !== null

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
                <span className="connector-badge">GitHub</span>
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
            aria-labelledby="create-source-title"
            onKeyDown={(event) => {
              if (event.key === 'Escape') {
                closeCreateModal()
              }
            }}
          >
            <div className="modal-header">
              <h3 id="create-source-title">Create Source</h3>
              <button
                className="icon-button"
                type="button"
                aria-label="Close create Source dialog"
                disabled={submitting}
                onClick={closeCreateModal}
              >
                ×
              </button>
            </div>

            <form className="source-form" onSubmit={handleCreate}>
              <label htmlFor="source-publisher">Publisher</label>
              <select
                id="source-publisher"
                name="publisherId"
                value={publisherId}
                disabled={submitting}
                autoFocus
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
                defaultValue="github"
                disabled={submitting}
              >
                <option value="github">GitHub</option>
              </select>

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

              <label htmlFor="source-name">Source name</label>
              <input
                id="source-name"
                name="sourceName"
                value={sourceName}
                disabled={submitting}
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

              {createFailed ? (
                <p className="request-error" role="alert">
                  Unable to create Source. Try again.
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
                  {submitting ? 'Creating…' : 'Create Source'}
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
