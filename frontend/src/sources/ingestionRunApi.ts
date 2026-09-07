export type IngestionRunStatus = 'RUNNING' | 'SUCCEEDED' | 'FAILED'

export type IngestionRun = {
  id: string
  sourceId: string
  status: IngestionRunStatus
  startedAt: string
  finishedAt: string | null
  failureCode: string | null
  failureMessage: string | null
}

export class IngestionRunApiError extends Error {
  readonly kind: 'http' | 'network'
  readonly status?: number

  constructor(
    message: string,
    kind: 'http' | 'network',
    status?: number,
  ) {
    super(message)
    this.name = 'IngestionRunApiError'
    this.kind = kind
    this.status = status
  }
}

export async function synchronizeSource(sourceId: string): Promise<IngestionRun> {
  const response = await fetch('/api/admin/ingestion-runs', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ sourceId }),
  }).catch(() => {
    throw new IngestionRunApiError(
      'Unable to reach the Ingestion Run API',
      'network',
    )
  })

  if (response.status !== 201) {
    throw new IngestionRunApiError(
      `Ingestion Run API returned status ${response.status}`,
      'http',
      response.status,
    )
  }

  return (await response.json()) as IngestionRun
}

export async function latestIngestionRun(
  sourceId: string,
): Promise<IngestionRun | null> {
  const response = await fetch(
    `/api/admin/ingestion-runs?sourceId=${encodeURIComponent(sourceId)}`,
  ).catch(() => {
    throw new IngestionRunApiError(
      'Unable to reach the Ingestion Run API',
      'network',
    )
  })

  if (response.status !== 200) {
    throw new IngestionRunApiError(
      `Ingestion Run API returned status ${response.status}`,
      'http',
      response.status,
    )
  }

  const runs = (await response.json()) as IngestionRun[]
  return runs[0] ?? null
}
