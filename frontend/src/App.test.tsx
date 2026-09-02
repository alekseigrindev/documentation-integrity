import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import App from './App'

vi.mock('./publishers/PublisherManagement', () => ({
  default: () => <div>Publisher management</div>,
}))

vi.mock('./sources/SourceManagement', () => ({
  default: () => <div>Source management</div>,
}))

describe('App', () => {
  it('switches between Source and Publisher management', () => {
    render(<App />)

    expect(
      screen.getByRole('heading', {
        level: 1,
        name: 'Documentation Integrity',
      }),
    ).toBeInTheDocument()
    expect(
      screen.getByRole('navigation', { name: 'Administration' }),
    ).toBeInTheDocument()
    const sourcesButton = screen.getByRole('button', { name: 'Sources' })
    const publishersButton = screen.getByRole('button', { name: 'Publishers' })

    expect(sourcesButton).toHaveAttribute('aria-pressed', 'true')
    expect(publishersButton).toHaveAttribute('aria-pressed', 'false')
    expect(screen.getByText('Source management')).toBeInTheDocument()
    expect(screen.queryByText('Publisher management')).not.toBeInTheDocument()

    fireEvent.click(publishersButton)

    expect(publishersButton).toHaveAttribute('aria-pressed', 'true')
    expect(sourcesButton).toHaveAttribute('aria-pressed', 'false')
    expect(screen.getByText('Publisher management')).toBeInTheDocument()
    expect(screen.queryByText('Source management')).not.toBeInTheDocument()
  })
})
