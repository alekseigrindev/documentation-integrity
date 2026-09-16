export type ApplicationSection =
  | 'search'
  | 'evaluations'
  | 'sources'
  | 'publishers'
  | 'ingestion-runs'

type NavigationProps = {
  activeSection: ApplicationSection
  onSectionSelect: (section: ApplicationSection) => void
}

function Navigation({ activeSection, onSectionSelect }: NavigationProps) {
  return (
    <aside className="sidebar">
      <nav aria-label="Application">
        <button
          className={
            activeSection === 'search'
              ? 'navigation-item navigation-item-active'
              : 'navigation-item'
          }
          type="button"
          aria-pressed={activeSection === 'search'}
          onClick={() => onSectionSelect('search')}
        >
          <svg
            className="navigation-icon"
            viewBox="0 0 24 24"
            aria-hidden="true"
          >
            <path d="m20.71 19.29-4.17-4.17A7.5 7.5 0 1 0 15.12 16l4.17 4.17 1.42-.88ZM5 10a5 5 0 1 1 10 0 5 5 0 0 1-10 0Z" />
          </svg>
          <span>Search</span>
        </button>
        <button
          className={
            activeSection === 'evaluations'
              ? 'navigation-item navigation-item-active'
              : 'navigation-item'
          }
          type="button"
          aria-pressed={activeSection === 'evaluations'}
          onClick={() => onSectionSelect('evaluations')}
        >
          <svg
            className="navigation-icon"
            viewBox="0 0 24 24"
            aria-hidden="true"
          >
            <path d="M4 19h16v2H4v-2Zm1-2V9h3v8H5Zm5 0V3h3v14h-3Zm5 0v-5h3v5h-3Z" />
          </svg>
          <span>Evaluations</span>
        </button>
        <button
          className={
            activeSection === 'sources'
              ? 'navigation-item navigation-item-active'
              : 'navigation-item'
          }
          type="button"
          aria-pressed={activeSection === 'sources'}
          onClick={() => onSectionSelect('sources')}
        >
          <svg
            className="navigation-icon"
            viewBox="0 0 24 24"
            aria-hidden="true"
          >
            <path d="M4 4h16v16H4V4Zm3 3v2h10V7H7Zm0 4v2h10v-2H7Zm0 4v2h6v-2H7Z" />
          </svg>
          <span>Sources</span>
        </button>
        <button
          className={
            activeSection === 'publishers'
              ? 'navigation-item navigation-item-active'
              : 'navigation-item'
          }
          type="button"
          aria-pressed={activeSection === 'publishers'}
          onClick={() => onSectionSelect('publishers')}
        >
          <svg
            className="navigation-icon"
            viewBox="0 0 24 24"
            aria-hidden="true"
          >
            <path d="M5 3h14a2 2 0 0 1 2 2v16H3V5a2 2 0 0 1 2-2Zm2 4v2h2V7H7Zm4 0v2h2V7h-2Zm4 0v2h2V7h-2ZM7 12v2h2v-2H7Zm4 0v2h2v-2h-2Zm4 0v2h2v-2h-2ZM9 17v4h6v-4H9Z" />
          </svg>
          <span>Publishers</span>
        </button>
        <button
          className={
            activeSection === 'ingestion-runs'
              ? 'navigation-item navigation-item-active'
              : 'navigation-item'
          }
          type="button"
          aria-pressed={activeSection === 'ingestion-runs'}
          onClick={() => onSectionSelect('ingestion-runs')}
        >
          <svg
            className="navigation-icon"
            viewBox="0 0 24 24"
            aria-hidden="true"
          >
            <path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2Zm1 11H7v-2h4V6h2Z" />
          </svg>
          <span>Ingestion runs</span>
        </button>
      </nav>
    </aside>
  )
}

export default Navigation
