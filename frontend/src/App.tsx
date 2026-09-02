import Navigation from './Navigation'
import PublisherManagement from './publishers/PublisherManagement'

function App() {
  return (
    <div className="app">
      <header className="app-header">
        <span className="brand-mark" aria-hidden="true">
          DI
        </span>
        <h1>Documentation Integrity</h1>
      </header>

      <div className="app-layout">
        <Navigation />

        <main className="main-content">
          <PublisherManagement />
        </main>
      </div>
    </div>
  )
}

export default App
