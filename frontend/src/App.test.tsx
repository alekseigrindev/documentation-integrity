import { render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import App from './App'
import { listPublishers } from './publishers/publisherApi'

vi.mock('./publishers/publisherApi', () => ({
  listPublishers: vi.fn(),
}))

beforeEach(() => {
  vi.mocked(listPublishers).mockReset()
})

describe('App', () => {
  it('shows Publishers as the current administration section', () => {
    vi.mocked(listPublishers).mockReturnValue(new Promise(() => {}))

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

  it('shows a loading state while Publishers are being loaded', () => {
    vi.mocked(listPublishers).mockReturnValue(new Promise(() => {}))

    render(<App />)

    expect(screen.getByRole('status')).toHaveTextContent(
      'Loading Publishers…',
    )
  })

  it('shows an empty state when no Publishers are registered', async () => {
    vi.mocked(listPublishers).mockResolvedValue([])

    render(<App />)

    expect(
      await screen.findByText('No Publishers are registered yet.'),
    ).toBeInTheDocument()
  })

  it('shows Publishers in the order returned by the API', async () => {
    vi.mocked(listPublishers).mockResolvedValue([
      {
        id: '53f81218-3017-4852-a65f-e68e79813436',
        name: 'Alpha Documentation',
      },
      {
        id: 'c27f646d-2a09-4239-b86f-f169988b80f8',
        name: 'Zulu Documentation',
      },
    ])

    render(<App />)

    const items = await screen.findAllByRole('listitem')

    expect(items.map((item) => item.textContent)).toEqual([
      'Alpha Documentation',
      'Zulu Documentation',
    ])
  })

  it('shows a request-error state when Publishers cannot be loaded', async () => {
    vi.mocked(listPublishers).mockRejectedValue(new Error('Network failure'))

    render(<App />)

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Unable to load Publishers.',
    )
  })
})
