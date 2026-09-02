import { useEffect, useState } from 'react'
import { listPublishers, type Publisher } from './publishers/publisherApi'

function App() {
  const [publishers, setPublishers] = useState<Publisher[] | null>(null)
  const [loadFailed, setLoadFailed] = useState(false)

  useEffect(() => {
    listPublishers().then(setPublishers).catch(() => setLoadFailed(true))
  }, [])

  return (
    <main className="app-shell">
      <section className="publisher-management" aria-labelledby="page-title">
        <h1 id="page-title">Documentation Integrity</h1>
        <h2>Publishers</h2>

        {loadFailed ? (
          <p className="request-error" role="alert">
            Unable to load Publishers.
          </p>
        ) : publishers === null ? (
          <p role="status">Loading Publishers…</p>
        ) : publishers.length === 0 ? (
          <p>No Publishers are registered yet.</p>
        ) : (
          <ul className="publisher-list" aria-label="Publishers">
            {publishers.map((publisher) => (
              <li key={publisher.id}>{publisher.name}</li>
            ))}
          </ul>
        )}
      </section>
    </main>
  )
}

export default App
