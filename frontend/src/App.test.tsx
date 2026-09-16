import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
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

describe('App', () => {
  it('switches between Search, Source, and Publisher management', () => {
    render(<App />)

    expect(
      screen.getByRole('heading', {
        level: 1,
        name: 'Documentation Integrity',
      }),
    ).toBeInTheDocument()
    expect(
      screen.getByRole('navigation', { name: 'Application' }),
    ).toBeInTheDocument()
    const sourcesButton = screen.getByRole('button', { name: 'Sources' })
    const searchButton = screen.getByRole('button', { name: 'Search' })
    const publishersButton = screen.getByRole('button', { name: 'Publishers' })
    const ingestionRunsButton = screen.getByRole('button', {
      name: 'Ingestion runs',
    })
    const evaluationsButton = screen.getByRole('button', {
      name: 'Evaluations',
    })

    expect(searchButton).toHaveAttribute('aria-pressed', 'true')
    expect(sourcesButton).toHaveAttribute('aria-pressed', 'false')
    expect(publishersButton).toHaveAttribute('aria-pressed', 'false')
    expect(ingestionRunsButton).toHaveAttribute('aria-pressed', 'false')
    expect(evaluationsButton).toHaveAttribute('aria-pressed', 'false')
    expect(screen.getByText('Documentation search')).toBeInTheDocument()
    expect(screen.queryByText('Publisher management')).not.toBeInTheDocument()

    fireEvent.click(evaluationsButton)

    expect(evaluationsButton).toHaveAttribute('aria-pressed', 'true')
    expect(searchButton).toHaveAttribute('aria-pressed', 'false')
    expect(screen.getByText('Evaluation management')).toBeInTheDocument()

    fireEvent.click(searchButton)

    fireEvent.click(sourcesButton)

    expect(sourcesButton).toHaveAttribute('aria-pressed', 'true')
    expect(searchButton).toHaveAttribute('aria-pressed', 'false')
    expect(screen.getByText('Source management')).toBeInTheDocument()

    fireEvent.click(publishersButton)

    expect(publishersButton).toHaveAttribute('aria-pressed', 'true')
    expect(sourcesButton).toHaveAttribute('aria-pressed', 'false')
    expect(screen.getByText('Publisher management')).toBeInTheDocument()
    expect(screen.queryByText('Source management')).not.toBeInTheDocument()

    fireEvent.click(ingestionRunsButton)

    expect(ingestionRunsButton).toHaveAttribute('aria-pressed', 'true')
    expect(publishersButton).toHaveAttribute('aria-pressed', 'false')
    expect(screen.getByText('Ingestion run management')).toBeInTheDocument()
    expect(screen.queryByText('Publisher management')).not.toBeInTheDocument()
  })
})
