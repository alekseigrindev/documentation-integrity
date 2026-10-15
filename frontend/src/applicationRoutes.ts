export const sectionPaths = {
  search: '/search',
  evaluations: '/evaluations',
  sources: '/sources',
  publishers: '/publishers',
  'ingestion-runs': '/ingestion-runs',
} as const

export type ApplicationSection = keyof typeof sectionPaths

export function sectionFromPath(pathname: string): ApplicationSection | null {
  const path = pathname.replace(/\/+$/, '') || '/'

  if (path === '/') {
    return 'search'
  }

  return (Object.keys(sectionPaths) as ApplicationSection[])
    .find((section) => sectionPaths[section] === path) ?? null
}
