import { useState } from 'react'
import Navigation from './Navigation'
import type { ApplicationSection } from './Navigation'
import DocumentationSearch from './search/DocumentationSearch'
import PublisherManagement from './publishers/PublisherManagement'
import IngestionRuns from './ingestion-runs/IngestionRuns'
import SourceManagement from './sources/SourceManagement'
import EvaluationManagement from './evaluation/EvaluationManagement'

function App() {
  const [activeSection, setActiveSection] =
    useState<ApplicationSection>('search')

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
          onSectionSelect={setActiveSection}
        />

        <main className="main-content">
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
