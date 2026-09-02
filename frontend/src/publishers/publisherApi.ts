export type Publisher = {
    id: string
    name: string
}

export type CreatePublisherRequest = {
    name: string
}

export class PublisherApiError extends Error {
    readonly kind: 'http' | 'network'
    readonly status?: number

    constructor(
        message: string,
        kind: 'http' | 'network',
        status?: number,
    ) {
        super(message)
        this.name = 'PublisherApiError'
        this.kind = kind
        this.status = status
    }
}

export async function createPublisher(
    request: CreatePublisherRequest,
): Promise<Publisher> {
    const response = await fetch('/api/admin/publishers', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(request),
    }).catch(() => {
        throw new PublisherApiError(
            'Unable to reach the Publisher API',
            'network',
        )
    })

    if (response.status !== 200 && response.status !== 201) {
        throw new PublisherApiError(
            `Publisher API returned status ${response.status}`,
            'http',
            response.status,
        )
    }

    return (await response.json()) as Publisher
}

export async function listPublishers(): Promise<Publisher[]> {
    const response = await fetch('/api/admin/publishers').catch(() => {
        throw new PublisherApiError(
            'Unable to reach the Publisher API',
            'network',
        )
    })

    if (response.status !== 200) {
        throw new PublisherApiError(
            `Publisher API returned status ${response.status}`,
            'http',
            response.status,
        )
    }

    return (await response.json()) as Publisher[]
}

