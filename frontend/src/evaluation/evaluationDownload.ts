import type { LexicalEvaluationReport } from './evaluationApi'

function safeFilePart(value: string) {
  return value
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9_-]+/g, '-')
    .replace(/^-+|-+$/g, '')
}

export function serializeEvaluationReport(report: LexicalEvaluationReport) {
  return JSON.stringify(report, null, 2)
}

export function downloadEvaluationReport(
  report: LexicalEvaluationReport,
  sourceKey: string,
) {
  const blob = new Blob([serializeEvaluationReport(report)], {
    type: 'application/json',
  })
  const downloadUrl = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  const fileSource = safeFilePart(sourceKey) || report.sourceId

  anchor.href = downloadUrl
  anchor.download =
    `lexical-evaluation-${fileSource}` +
    `-v${report.evaluationSetVersion}.json`
  document.body.append(anchor)
  anchor.click()
  anchor.remove()
  URL.revokeObjectURL(downloadUrl)
}
