export type AdministrationSection = 'sources' | 'publishers'

type NavigationProps = {
  activeSection: AdministrationSection
  onSectionSelect: (section: AdministrationSection) => void
}

function Navigation({ activeSection, onSectionSelect }: NavigationProps) {
  return (
    <aside className="sidebar">
      <nav aria-label="Administration">
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
      </nav>
    </aside>
  )
}

export default Navigation
