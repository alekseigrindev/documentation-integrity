function Navigation() {
  return (
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
  )
}

export default Navigation
