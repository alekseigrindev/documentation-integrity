export type Connector = {
  type: string
  description: string
}

export class ConnectorApiError extends Error {
  readonly kind: 'http' | 'network'
  readonly status?: number

  constructor(
    message: string,
    kind: 'http' | 'network',
    status?: number,
  ) {
    super(message)
    this.name = 'ConnectorApiError'
    this.kind = kind
    this.status = status
  }
}

export async function listConnectors(): Promise<Connector[]> {
  const response = await fetch('/api/admin/connectors').catch(() => {
    throw new ConnectorApiError('Unable to reach the Connector API', 'network')
  })

  if (response.status !== 200) {
    throw new ConnectorApiError(
      `Connector API returned status ${response.status}`,
      'http',
      response.status,
    )
  }

  return (await response.json()) as Connector[]
}
