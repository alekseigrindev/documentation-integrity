import { useEffect, useState } from 'react'
import Navigation from './Navigation'
import { sectionFromPath, sectionPaths } from './applicationRoutes'
import type { ApplicationSection } from './applicationRoutes'
import DocumentationSearch from './search/DocumentationSearch'
import PublisherManagement from './publishers/PublisherManagement'
import IngestionRuns from './ingestion-runs/IngestionRuns'
import SourceManagement from './sources/SourceManagement'
import EvaluationManagement from './evaluation/EvaluationManagement'

function App() {
  const [pathname, setPathname] = useState(() => window.location.pathname)
  const activeSection = sectionFromPath(pathname)

  useEffect(() => {
    const onPopState = () => setPathname(window.location.pathname)
    window.addEventListener('popstate', onPopState)
    return () => window.removeEventListener('popstate', onPopState)
  }, [])

  function navigate(section: ApplicationSection) {
    const nextPath = sectionPaths[section]
    if (window.location.pathname !== nextPath) {
      window.history.pushState(null, '', nextPath)
    }
    setPathname(nextPath)
  }

  return (
    <div className="app">
      <header className="app-header">
        <span className="brand-mark" aria-hidden="true">
          DI
        </span>
        <h1>Documentation Integrity</h1>
      </header>

      <div className="app-layout">
        <Navigation
          activeSection={activeSection}
          onSectionSelect={navigate}
        />

        <main className="main-content">
          {activeSection === null ? <h2>Page not found</h2> : null}
          {activeSection === 'search' ? <DocumentationSearch /> : null}
          {activeSection === 'evaluations' ? <EvaluationManagement /> : null}
          {activeSection === 'sources' ? <SourceManagement /> : null}
          {activeSection === 'publishers' ? <PublisherManagement /> : null}
          {activeSection === 'ingestion-runs' ? <IngestionRuns /> : null}
        </main>
      </div>
    </div>
  )
}

export default App
