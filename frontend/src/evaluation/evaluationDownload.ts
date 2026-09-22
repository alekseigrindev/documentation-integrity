import type { RetrievalMethod } from '../search/searchApi'
import type { EvaluationReport } from './evaluationApi'

function safeFilePart(value: string) {
  return value
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9_-]+/g, '-')
    .replace(/^-+|-+$/g, '')
}

export function serializeEvaluationReport(
  report: EvaluationReport,
  retrievalMethod: RetrievalMethod,
) {
  return JSON.stringify({ ...report, retrievalMethod }, null, 2)
}

export function downloadEvaluationReport(
  report: EvaluationReport,
  sourceKey: string,
  retrievalMethod: RetrievalMethod,
) {
  const blob = new Blob([serializeEvaluationReport(report, retrievalMethod)], {
    type: 'application/json',
  })
  const downloadUrl = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  const fileSource = safeFilePart(sourceKey) || report.sourceId

  anchor.href = downloadUrl
  anchor.download =
    `${retrievalMethod.toLowerCase()}-evaluation-${fileSource}` +
    `-v${report.evaluationSetVersion}.json`
  document.body.append(anchor)
  anchor.click()
  anchor.remove()
  URL.revokeObjectURL(downloadUrl)
}
