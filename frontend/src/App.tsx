import { useState } from 'react'
import Navigation from './Navigation'
import type { AdministrationSection } from './Navigation'
import PublisherManagement from './publishers/PublisherManagement'
import SourceManagement from './sources/SourceManagement'

function App() {
  const [activeSection, setActiveSection] =
    useState<AdministrationSection>('sources')

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
          {activeSection === 'sources' ? (
            <SourceManagement />
          ) : (
            <PublisherManagement />
          )}
        </main>
      </div>
    </div>
  )
}

export default App
