import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import DocumentationSearch from './DocumentationSearch'
import { searchDocumentation } from './searchApi'

vi.mock('./searchApi', () => ({
  searchDocumentation: vi.fn(),
}))

const matches = [
  {
    chunkId: '53f81218-3017-4852-a65f-e68e79813436',
    content: 'First matching passage',
    sourceLocator: 'first.md',
    canonicalUrl: null,
    attribution: 'GitHub Docs',
  },
  {
    chunkId: 'f59642e6-627f-434e-aa34-eb36b387ad46',
    content: 'Second matching passage',
    sourceLocator: 'second.md',
    canonicalUrl: null,
    attribution: 'GitHub Docs',
  },
]

describe('DocumentationSearch', () => {
  it('shows the number of passages returned by a completed search', async () => {
    vi.mocked(searchDocumentation).mockResolvedValue(matches)

    render(<DocumentationSearch />)

    fireEvent.change(screen.getByRole('searchbox', { name: 'Search query' }), {
      target: { value: 'write permission' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Search' }))

    expect(await screen.findByText('2 matching passages')).toBeInTheDocument()
  })
})
