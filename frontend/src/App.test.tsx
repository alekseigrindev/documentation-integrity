import { act, fireEvent, render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import App from './App'

vi.mock('./publishers/PublisherManagement', () => ({
  default: () => <div>Publisher management</div>,
}))

vi.mock('./sources/SourceManagement', () => ({
  default: () => <div>Source management</div>,
}))

vi.mock('./search/DocumentationSearch', () => ({
  default: () => <div>Documentation search</div>,
}))

vi.mock('./ingestion-runs/IngestionRuns', () => ({
  default: () => <div>Ingestion run management</div>,
}))

vi.mock('./evaluation/EvaluationManagement', () => ({
  default: () => <div>Evaluation management</div>,
}))

const sections = [
  ['/search', 'Search', 'Documentation search'],
  ['/evaluations', 'Evaluations', 'Evaluation management'],
  ['/sources', 'Sources', 'Source management'],
  ['/publishers', 'Publishers', 'Publisher management'],
  ['/ingestion-runs', 'Ingestion runs', 'Ingestion run management'],
] as const

describe('App routing', () => {
  beforeEach(() => {
    window.history.replaceState(null, '', '/')
  })

  it('opens Search at the root URL', () => {
    render(<App />)

    expect(screen.getByRole('heading', {
      level: 1,
      name: 'Documentation Integrity',
    })).toBeInTheDocument()
    expect(screen.getByRole('navigation', { name: 'Application' }))
      .toBeInTheDocument()
    expect(screen.getByText('Documentation search')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Search' }))
      .toHaveAttribute('aria-current', 'page')
  })

  it.each(sections)('opens %s directly and keeps it when remounted', (path, label, content) => {
    window.history.replaceState(null, '', path)
    const view = render(<App />)

    expect(screen.getByText(content)).toBeInTheDocument()
    expect(screen.getByRole('link', { name: label }))
      .toHaveAttribute('aria-current', 'page')

    view.unmount()
    render(<App />)

    expect(window.location.pathname).toBe(path)
    expect(screen.getByText(content)).toBeInTheDocument()
  })

  it('updates the URL and active link when switching sections', () => {
    render(<App />)

    for (const [path, label, content] of sections) {
      const link = screen.getByRole('link', { name: label })
      expect(link).toHaveAttribute('href', path)
      fireEvent.click(link)

      expect(window.location.pathname).toBe(path)
      expect(link).toHaveAttribute('aria-current', 'page')
      expect(screen.getByText(content)).toBeInTheDocument()
      expect(screen.getAllByRole('link', { current: 'page' })).toHaveLength(1)
    }
  })

  it('follows browser Back and Forward', async () => {
    render(<App />)
    fireEvent.click(screen.getByRole('link', { name: 'Sources' }))
    fireEvent.click(screen.getByRole('link', { name: 'Evaluations' }))

    await act(async () => {
      const navigation = new Promise<void>((resolve) => {
        window.addEventListener('popstate', () => resolve(), { once: true })
      })
      window.history.back()
      await navigation
    })

    expect(window.location.pathname).toBe('/sources')
    expect(screen.getByText('Source management')).toBeInTheDocument()

    await act(async () => {
      const navigation = new Promise<void>((resolve) => {
        window.addEventListener('popstate', () => resolve(), { once: true })
      })
      window.history.forward()
      await navigation
    })

    expect(window.location.pathname).toBe('/evaluations')
    expect(screen.getByText('Evaluation management')).toBeInTheDocument()
  })

  it('does not intercept modified link clicks', () => {
    render(<App />)
    const link = screen.getByRole('link', { name: 'Sources' })
    // Suppress jsdom's unsupported native navigation after checking the event.
    let preventedByApp = true
    const preventNativeNavigation = (event: MouseEvent) => {
      preventedByApp = event.defaultPrevented
      event.preventDefault()
    }
    document.addEventListener('click', preventNativeNavigation)
    try {
      fireEvent.click(link, { ctrlKey: true })
      expect(preventedByApp).toBe(false)
      expect(window.location.pathname).toBe('/')
    } finally {
      document.removeEventListener('click', preventNativeNavigation)
    }
  })

  it('accepts a trailing slash', () => {
    window.history.replaceState(null, '', '/sources/')
    render(<App />)
    expect(screen.getByText('Source management')).toBeInTheDocument()
  })

  it('shows an unknown-page message instead of silently opening Search', () => {
    window.history.replaceState(null, '', '/unknown')
    render(<App />)

    expect(screen.getByRole('heading', { name: 'Page not found' }))
      .toBeInTheDocument()
    expect(screen.queryByText('Documentation search')).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { current: 'page' })).not.toBeInTheDocument()

    fireEvent.click(screen.getByRole('link', { name: 'Search' }))
    expect(window.location.pathname).toBe('/search')
    expect(screen.getByText('Documentation search')).toBeInTheDocument()
  })
})
