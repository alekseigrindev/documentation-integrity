export type ConnectorType = string

export type Source = {
  id: string
  publisherId: string
  connectorType: ConnectorType
  sourceKey: string
  name: string
  description?: string
  sourceUrl?: string
  licenseName?: string
  licenseUrl?: string
  accessPolicyUrl?: string
}

export type CreateSourceRequest = {
  publisherId: string
  connectorType: ConnectorType
  sourceKey: string
  name: string
  sourceUrl?: string
}

type LocalDirectorySelectionResponse = {
  sourceUrl: string | null
}

export class SourceApiError extends Error {
  readonly kind: 'http' | 'network'
  readonly status?: number

  constructor(
    message: string,
    kind: 'http' | 'network',
    status?: number,
  ) {
    super(message)
    this.name = 'SourceApiError'
    this.kind = kind
    this.status = status
  }
}

export async function listSources(): Promise<Source[]> {
  const response = await fetch('/api/admin/sources').catch(() => {
    throw new SourceApiError('Unable to reach the Source API', 'network')
  })

  if (response.status !== 200) {
    throw new SourceApiError(
      `Source API returned status ${response.status}`,
      'http',
      response.status,
    )
  }

  return (await response.json()) as Source[]
}

export async function createSource(
  request: CreateSourceRequest,
): Promise<Source> {
  const response = await fetch('/api/admin/sources', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  }).catch(() => {
    throw new SourceApiError('Unable to reach the Source API', 'network')
  })

  if (response.status !== 200 && response.status !== 201) {
    throw new SourceApiError(
      `Source API returned status ${response.status}`,
      'http',
      response.status,
    )
  }

  return (await response.json()) as Source
}

export async function updateSource(
  sourceId: string,
  request: CreateSourceRequest,
): Promise<Source> {
  const response = await fetch(`/api/admin/sources/${sourceId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  }).catch(() => {
    throw new SourceApiError('Unable to reach the Source API', 'network')
  })

  if (response.status !== 200) {
    throw new SourceApiError(
      `Source API returned status ${response.status}`,
      'http',
      response.status,
    )
  }

  return (await response.json()) as Source
}

export async function chooseLocalDirectory(): Promise<string | null> {
  const response = await fetch('/api/admin/local-directories/choose', {
    method: 'POST',
  }).catch(() => {
    throw new SourceApiError('Unable to reach the Source API', 'network')
  })

  if (response.status !== 200) {
    throw new SourceApiError(
      `Source API returned status ${response.status}`,
      'http',
      response.status,
    )
  }

  return ((await response.json()) as LocalDirectorySelectionResponse).sourceUrl
}
