import { fireEvent, render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import DocumentationSearch from './DocumentationSearch'
import { listRetrievalMethods, searchDocumentation } from './searchApi'
import { listSources } from '../sources/sourceApi'

vi.mock('./searchApi', () => ({
  listRetrievalMethods: vi.fn(),
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
    vi.mocked(listRetrievalMethods).mockReset()
    vi.mocked(searchDocumentation).mockReset()
    vi.mocked(listSources).mockResolvedValue([])
    vi.mocked(listRetrievalMethods).mockResolvedValue([
      { retrievalMethod: 'LEXICAL', displayName: 'Lexical search' },
      { retrievalMethod: 'VECTOR', displayName: 'Vector search' },
      { retrievalMethod: 'HYBRID', displayName: 'Hybrid search' },
    ])
  })

  it('shows the number of passages returned by a completed search', async () => {
    vi.mocked(searchDocumentation).mockResolvedValue(matches)

    render(<DocumentationSearch />)

    await screen.findByRole('option', { name: 'Lexical search' })
    fireEvent.change(screen.getByRole('searchbox', { name: 'Search query' }), {
      target: { value: 'write permission' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Search' }))

    expect(await screen.findByText('2 passages')).toBeInTheDocument()
    expect(searchDocumentation).toHaveBeenCalledWith(
      'write permission',
      new Set(),
      'LEXICAL',
    )
  })

  it('searches only the selected Sources', async () => {
    vi.mocked(listSources).mockResolvedValue([source])
    vi.mocked(searchDocumentation).mockResolvedValue(matches)

    render(<DocumentationSearch />)

    await screen.findByRole('option', { name: 'Lexical search' })
    fireEvent.click(await screen.findByText('All Sources'))
    fireEvent.click(await screen.findByRole('checkbox', { name: 'GitHub Docs' }))
    fireEvent.change(screen.getByRole('searchbox', { name: 'Search query' }), {
      target: { value: 'write permission' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Search' }))

    await screen.findByText('2 passages')

    expect(searchDocumentation).toHaveBeenCalledWith(
      'write permission',
      new Set([source.id]),
      'LEXICAL',
    )
  })

  it('sends the selected vector method with the search request', async () => {
    vi.mocked(searchDocumentation).mockResolvedValue(matches)

    render(<DocumentationSearch />)

    fireEvent.change(await screen.findByLabelText('Search method'), {
      target: { value: 'VECTOR' },
    })
    fireEvent.change(screen.getByRole('searchbox', { name: 'Search query' }), {
      target: { value: 'larger runners' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Search' }))

    expect(await screen.findByText('2 passages')).toBeInTheDocument()
    expect(searchDocumentation).toHaveBeenCalledWith(
      'larger runners',
      new Set(),
      'VECTOR',
    )
  })

  it('sends the selected hybrid method with the search request', async () => {
    vi.mocked(searchDocumentation).mockResolvedValue(matches)

    render(<DocumentationSearch />)

    fireEvent.change(await screen.findByLabelText('Search method'), {
      target: { value: 'HYBRID' },
    })
    fireEvent.change(screen.getByRole('searchbox', { name: 'Search query' }), {
      target: { value: 'failed workflow' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Search' }))

    expect(await screen.findByText('2 passages')).toBeInTheDocument()
    expect(searchDocumentation).toHaveBeenCalledWith(
      'failed workflow',
      new Set(),
      'HYBRID',
    )
  })

  it('closes the Source selector when clicking outside it', async () => {
    vi.mocked(listSources).mockResolvedValue([source])

    render(<DocumentationSearch />)

    const sourceMenu = (await screen.findByText('All Sources')).closest(
      'details',
    )

    fireEvent.click(screen.getByText('All Sources'))
    expect(sourceMenu).toHaveAttribute('open')

    fireEvent.pointerDown(screen.getByRole('heading', {
      name: 'Search documentation',
    }))

    expect(sourceMenu).not.toHaveAttribute('open')
  })

  it('collapses and expands a long matching passage', async () => {
    const longContent = `Start ${'long passage '.repeat(60)}End`
    vi.mocked(searchDocumentation).mockResolvedValue([
      {
        ...matches[0],
        content: longContent,
      },
    ])

    render(<DocumentationSearch />)

    await screen.findByRole('option', { name: 'Lexical search' })
    fireEvent.change(screen.getByRole('searchbox', { name: 'Search query' }), {
      target: { value: 'write permission' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Search' }))

    const expandButton = await screen.findByRole('button', { name: 'Expand' })
    const content = document.querySelector('.search-result-content')

    expect(expandButton).toHaveAttribute('aria-expanded', 'false')
    expect(content?.textContent).toMatch(/…$/)
    expect(content?.textContent).not.toBe(longContent)

    fireEvent.click(expandButton)

    expect(screen.getByRole('button', { name: 'Collapse' })).toHaveAttribute(
      'aria-expanded',
      'true',
    )
    expect(content?.textContent).toBe(longContent)
  })
})
