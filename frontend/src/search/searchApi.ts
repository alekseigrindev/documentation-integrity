export type SearchMatch = {
  chunkId: string
  content: string
  sourceLocator: string
  canonicalUrl: string | null
  attribution: string
}

type SearchResponse = {
  matches: SearchMatch[]
}

export class SearchApiError extends Error {
  readonly kind: 'http' | 'network'
  readonly status?: number

  constructor(
    message: string,
    kind: 'http' | 'network',
    status?: number,
  ) {
    super(message)
    this.name = 'SearchApiError'
    this.kind = kind
    this.status = status
  }
}

export async function searchDocumentation(query: string): Promise<SearchMatch[]> {
  const response = await fetch(
    `/api/documents/search?q=${encodeURIComponent(query)}`,
  ).catch(() => {
    throw new SearchApiError(
      'Unable to reach the Documentation Search API',
      'network',
    )
  })

  if (response.status !== 200) {
    throw new SearchApiError(
      `Documentation Search API returned status ${response.status}`,
      'http',
      response.status,
    )
  }

  return ((await response.json()) as SearchResponse).matches
}
