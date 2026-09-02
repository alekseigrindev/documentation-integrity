import { useEffect, useState } from 'react'
import { listPublishers, type Publisher } from './publishers/publisherApi'

function App() {
  const [publishers, setPublishers] = useState<Publisher[] | null>(null)
  const [loadFailed, setLoadFailed] = useState(false)

  useEffect(() => {
    listPublishers().then(setPublishers).catch(() => setLoadFailed(true))
  }, [])

  return (
    <div className="app">
      <header className="app-header">
        <span className="brand-mark" aria-hidden="true">
          DI
        </span>
        <h1>Documentation Integrity</h1>
      </header>

      <div className="app-layout">
        <aside className="sidebar">
          <nav aria-label="Administration">
            <a
              className="navigation-item navigation-item-active"
              href="#publishers"
              aria-current="page"
            >
              <svg
                className="navigation-icon"
                viewBox="0 0 24 24"
                aria-hidden="true"
              >
                <path d="M5 3h14a2 2 0 0 1 2 2v16H3V5a2 2 0 0 1 2-2Zm2 4v2h2V7H7Zm4 0v2h2V7h-2Zm4 0v2h2V7h-2ZM7 12v2h2v-2H7Zm4 0v2h2v-2h-2Zm4 0v2h2v-2h-2ZM9 17v4h6v-4H9Z" />
              </svg>
              <span>Publishers</span>
            </a>
          </nav>
        </aside>

        <main className="main-content">
          <section
            id="publishers"
            className="publisher-management"
            aria-labelledby="publishers-title"
          >
            <div className="section-heading">
              <h2 id="publishers-title">Publishers</h2>
              <p className="section-description">
                Organizations and teams responsible for approved documentation.
              </p>
            </div>

            <div className="content-panel">
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
            </div>
          </section>
        </main>
      </div>
    </div>
  )
}

export default App
