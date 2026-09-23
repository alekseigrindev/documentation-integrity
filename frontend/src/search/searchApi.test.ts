import { afterEach, describe, expect, it, vi } from 'vitest'
import { listRetrievalMethods, searchDocumentation } from './searchApi'

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('listRetrievalMethods', () => {
  it('returns only the methods offered by the backend', async () => {
    const methods = [
      { retrievalMethod: 'LEXICAL', displayName: 'Lexical search' },
    ]
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify(methods), { status: 200 }),
    )
    vi.stubGlobal('fetch', fetchMock)

    await expect(listRetrievalMethods()).resolves.toEqual(methods)
    expect(fetchMock).toHaveBeenCalledWith(
      '/api/documents/retrieval-methods',
    )
  })
})

describe('searchDocumentation', () => {
  it('sends the selected method and Source IDs to the backend', async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ matches: [] }), { status: 200 }),
    )
    vi.stubGlobal('fetch', fetchMock)

    await expect(searchDocumentation(
      'larger runners',
      ['source-a', 'source-b'],
      'VECTOR',
    )).resolves.toEqual([])

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/documents/search?q=larger+runners&method=VECTOR&sourceId=source-a&sourceId=source-b',
    )
  })
})
