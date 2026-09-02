import { type FormEvent, useEffect, useState } from 'react'
import {
  createPublisher,
  listPublishers,
  type Publisher,
} from './publisherApi'

function orderPublishers(publishers: Publisher[]) {
  return [...publishers].sort((left, right) => {
    const nameOrder = left.name.localeCompare(right.name)
    return nameOrder !== 0 ? nameOrder : left.id.localeCompare(right.id)
  })
}

function PublisherManagement() {
  const [publishers, setPublishers] = useState<Publisher[] | null>(null)
  const [loadFailed, setLoadFailed] = useState(false)
  const [publisherName, setPublisherName] = useState('')
  const [validationError, setValidationError] = useState<string | null>(null)
  const [createFailed, setCreateFailed] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [createOpen, setCreateOpen] = useState(false)

  useEffect(() => {
    listPublishers().then(setPublishers).catch(() => setLoadFailed(true))
  }, [])

  function openCreateModal() {
    setValidationError(null)
    setCreateFailed(false)
    setCreateOpen(true)
  }

  function closeCreateModal() {
    if (submitting) {
      return
    }

    setCreateOpen(false)
    setPublisherName('')
    setValidationError(null)
    setCreateFailed(false)
  }

  async function handleCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const name = publisherName.trim()
    if (!name) {
      setValidationError('Publisher name is required.')
      return
    }

    setValidationError(null)
    setCreateFailed(false)
    setSubmitting(true)

    try {
      const publisher = await createPublisher({ name })
      setPublishers((current) =>
        orderPublishers([
          ...(current ?? []).filter((item) => item.id !== publisher.id),
          publisher,
        ]),
      )
      setPublisherName('')
      setCreateOpen(false)
    } catch {
      setCreateFailed(true)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section
      id="publishers"
      className="publisher-management"
      aria-labelledby="publishers-title"
    >
      <div className="section-heading">
        <div>
          <h2 id="publishers-title">Publishers</h2>
          <p className="section-description">
            Organizations and teams responsible for approved documentation.
          </p>
        </div>
        {!loadFailed && publishers !== null ? (
          <button
            className="primary-button"
            type="button"
            onClick={openCreateModal}
          >
            Create Publisher
          </button>
        ) : null}
      </div>

      <div className="content-panel">
        {loadFailed ? (
          <p className="request-error" role="alert">
            Unable to load Publishers.
          </p>
        ) : publishers === null ? (
          <p role="status">Loading Publishers…</p>
        ) : publishers.length === 0 ? (
          <p className="empty-state">No Publishers are registered yet.</p>
        ) : (
          <ul className="publisher-list" aria-label="Publishers">
            {publishers.map((publisher) => (
              <li key={publisher.id}>{publisher.name}</li>
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
            aria-labelledby="create-publisher-title"
            onKeyDown={(event) => {
              if (event.key === 'Escape') {
                closeCreateModal()
              }
            }}
          >
            <div className="modal-header">
              <h3 id="create-publisher-title">Create Publisher</h3>
              <button
                className="icon-button"
                type="button"
                aria-label="Close create Publisher dialog"
                disabled={submitting}
                onClick={closeCreateModal}
              >
                ×
              </button>
            </div>

            <form className="publisher-form" onSubmit={handleCreate}>
              <label htmlFor="publisher-name">Publisher name</label>
              <input
                id="publisher-name"
                name="publisherName"
                value={publisherName}
                disabled={submitting}
                autoFocus
                aria-invalid={validationError !== null}
                aria-describedby={
                  validationError ? 'publisher-name-error' : undefined
                }
                onChange={(event) => {
                  setPublisherName(event.target.value)
                  setValidationError(null)
                  setCreateFailed(false)
                }}
              />
              {validationError ? (
                <p id="publisher-name-error" className="field-error">
                  {validationError}
                </p>
              ) : null}
              {createFailed ? (
                <p className="request-error" role="alert">
                  Unable to create Publisher. Try again.
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
                  {submitting ? 'Creating…' : 'Create Publisher'}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </section>
  )
}

export default PublisherManagement
