import { fireEvent, render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import DocumentationSearch from './DocumentationSearch'
import { searchDocumentation } from './searchApi'
import { listSources } from '../sources/sourceApi'

vi.mock('./searchApi', () => ({
  searchDocumentation: vi.fn(),
}))

vi.mock('../sources/sourceApi', () => ({
  listSources: vi.fn(),
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

const source = {
  id: 'f59642e6-627f-434e-aa34-eb36b387ad46',
  publisherId: '53f81218-3017-4852-a65f-e68e79813436',
  connectorType: 'github',
  sourceKey: 'github-docs',
  name: 'GitHub Docs',
}

describe('DocumentationSearch', () => {
  beforeEach(() => {
    vi.mocked(listSources).mockReset()
    vi.mocked(searchDocumentation).mockReset()
    vi.mocked(listSources).mockResolvedValue([])
  })

  it('shows the number of passages returned by a completed search', async () => {
    vi.mocked(searchDocumentation).mockResolvedValue(matches)

    render(<DocumentationSearch />)

    fireEvent.change(screen.getByRole('searchbox', { name: 'Search query' }), {
      target: { value: 'write permission' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Search' }))

    expect(await screen.findByText('2 matching passages')).toBeInTheDocument()
    expect(searchDocumentation).toHaveBeenCalledWith('write permission', new Set())
  })

  it('searches only the selected Sources', async () => {
    vi.mocked(listSources).mockResolvedValue([source])
    vi.mocked(searchDocumentation).mockResolvedValue(matches)

    render(<DocumentationSearch />)

    fireEvent.click(await screen.findByRole('checkbox', { name: 'GitHub Docs' }))
    fireEvent.change(screen.getByRole('searchbox', { name: 'Search query' }), {
      target: { value: 'write permission' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Search' }))

    await screen.findByText('2 matching passages')

    expect(searchDocumentation).toHaveBeenCalledWith(
      'write permission',
      new Set([source.id]),
    )
  })
})
