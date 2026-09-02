import { afterEach, describe, expect, it, vi } from 'vitest'
import { createPublisher, listPublishers, PublisherApiError } from './publisherApi'

afterEach(() => {
    vi.unstubAllGlobals()
})

describe('createPublisher', () => {
    it('creates and returns a new Publisher', async () => {
        const request = {
            name: 'GitHub Docs',
        }
        const publisher = {
            id: 'c27f646d-2a09-4239-b86f-f169988b80f8',
            name: 'GitHub Docs',
        }

        const fetchMock = vi.fn().mockResolvedValue(
            new Response(JSON.stringify(publisher), {
                status: 201,
                headers: {
                    'Content-Type': 'application/json',
                },
            }),
        )
        vi.stubGlobal('fetch', fetchMock)

        const result = await createPublisher(request)

        expect(fetchMock).toHaveBeenCalledOnce()
        expect(fetchMock).toHaveBeenCalledWith(
            '/api/admin/publishers',
            {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(request),
            },
        )
        expect(result).toEqual(publisher)
    })

    it('throws an HTTP error when the backend responds unsuccessfully', async () => {
        const fetchMock = vi.fn().mockResolvedValue(
            new Response(null, {
                status: 500,
            }),
        )
        vi.stubGlobal('fetch', fetchMock)

        const request = createPublisher({
            name: 'GitHub Docs',
        })

        await expect(request).rejects.toBeInstanceOf(PublisherApiError)
        await expect(request).rejects.toMatchObject({
            kind: 'http',
            status: 500,
        })
    })

    it('returns an existing Publisher when the backend responds with 200', async () => {
        const publisher = {
            id: 'c27f646d-2a09-4239-b86f-f169988b80f8',
            name: 'GitHub Docs',
        }

        const fetchMock = vi.fn().mockResolvedValue(
            new Response(JSON.stringify(publisher), {
                status: 200,
                headers: {
                    'Content-Type': 'application/json',
                },
            }),
        )
        vi.stubGlobal('fetch', fetchMock)

        const result = await createPublisher({
            name: 'GitHub Docs',
        })

        expect(result).toEqual(publisher)
    })

    it('throws a network error when no response is received', async () => {
        const fetchMock = vi.fn().mockRejectedValue(
            new TypeError('Failed to fetch'),
        )
        vi.stubGlobal('fetch', fetchMock)

        const operation = createPublisher({
            name: 'GitHub Docs',
        })

        await expect(operation).rejects.toBeInstanceOf(PublisherApiError)
        await expect(operation).rejects.toMatchObject({
            kind: 'network',
            status: undefined,
        })
    })
})

describe('listPublishers', () => {
    it('requests and returns Publishers', async () => {
        const publishers = [
            {
                id: 'c27f646d-2a09-4239-b86f-f169988b80f8',
                name: 'GitHub Docs',
            },
        ]

        const fetchMock = vi.fn().mockResolvedValue(
            new Response(JSON.stringify(publishers), {
                status: 200,
                headers: {
                    'Content-Type': 'application/json',
                },
            }),
        )
        vi.stubGlobal('fetch', fetchMock)

        const result = await listPublishers()

        expect(fetchMock).toHaveBeenCalledOnce()
        expect(fetchMock).toHaveBeenCalledWith('/api/admin/publishers')
        expect(result).toEqual(publishers)
    })

    it('throws an HTTP error when the backend responds unsuccessfully', async () => {
        const fetchMock = vi.fn().mockResolvedValue(
            new Response(null, {
                status: 500,
            }),
        )
        vi.stubGlobal('fetch', fetchMock)

        const request = listPublishers()

        await expect(request).rejects.toBeInstanceOf(PublisherApiError)
        await expect(request).rejects.toMatchObject({
            kind: 'http',
            status: 500,
        })
    })

    it('throws a network error when no response is received', async () => {
        const fetchMock = vi.fn().mockRejectedValue(
            new TypeError('Failed to fetch'),
        )
        vi.stubGlobal('fetch', fetchMock)

        const request = listPublishers()

        await expect(request).rejects.toBeInstanceOf(PublisherApiError)
        await expect(request).rejects.toMatchObject({
            kind: 'network',
            status: undefined,
        })
    })
})