import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import App from './App'

vi.mock('./publishers/PublisherManagement', () => ({
  default: () => <div>Publisher management</div>,
}))

describe('App', () => {
  it('shows Publishers as the current administration section', () => {
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
    expect(screen.getByRole('link', { name: 'Publishers' })).toHaveAttribute(
      'aria-current',
      'page',
    )
  })
})
