import { afterEach, describe, expect, it, vi } from 'vitest'
import { createSource, listSources, SourceApiError } from './sourceApi'

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('createSource', () => {
  const request = {
    publisherId: 'c27f646d-2a09-4239-b86f-f169988b80f8',
    connectorType: 'github' as const,
    sourceKey: 'github-docs',
    name: 'GitHub Docs',
  }

  const source = {
    id: '53f81218-3017-4852-a65f-e68e79813436',
    ...request,
  }

  it('creates and returns a new Source', async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify(source), {
        status: 201,
        headers: {
          'Content-Type': 'application/json',
        },
      }),
    )
    vi.stubGlobal('fetch', fetchMock)

    const result = await createSource(request)

    expect(fetchMock).toHaveBeenCalledWith('/api/admin/sources', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    })
    expect(result).toEqual(source)
  })

  it('returns an existing Source when the backend responds with 200', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        new Response(JSON.stringify(source), { status: 200 }),
      ),
    )

    await expect(createSource(request)).resolves.toEqual(source)
  })

  it('throws an HTTP error when the backend responds unsuccessfully', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(new Response(null, { status: 404 })),
    )

    await expect(createSource(request)).rejects.toMatchObject({
      kind: 'http',
      status: 404,
    })
  })

  it('throws a network error when no response is received', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockRejectedValue(new TypeError('Failed to fetch')),
    )

    await expect(createSource(request)).rejects.toMatchObject({
      kind: 'network',
      status: undefined,
    })
  })
})

describe('listSources', () => {
  const sources = [
    {
      id: '53f81218-3017-4852-a65f-e68e79813436',
      publisherId: 'c27f646d-2a09-4239-b86f-f169988b80f8',
      connectorType: 'github' as const,
      sourceKey: 'github-docs',
      name: 'GitHub Docs',
    },
  ]

  it('requests and returns Sources', async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify(sources), { status: 200 }),
    )
    vi.stubGlobal('fetch', fetchMock)

    await expect(listSources()).resolves.toEqual(sources)
    expect(fetchMock).toHaveBeenCalledWith('/api/admin/sources')
  })

  it('throws an HTTP error when the backend responds unsuccessfully', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(new Response(null, { status: 500 })),
    )

    const operation = listSources()

    await expect(operation).rejects.toBeInstanceOf(SourceApiError)
    await expect(operation).rejects.toMatchObject({
      kind: 'http',
      status: 500,
    })
  })

  it('throws a network error when no response is received', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockRejectedValue(new TypeError('Failed to fetch')),
    )

    await expect(listSources()).rejects.toMatchObject({
      kind: 'network',
      status: undefined,
    })
  })
})
