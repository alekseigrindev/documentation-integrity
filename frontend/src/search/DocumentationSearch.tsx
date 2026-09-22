import { type FormEvent, useEffect, useRef, useState } from 'react'
import { listSources, type Source } from '../sources/sourceApi'
import {
  listRetrievalMethods,
  searchDocumentation,
  type RetrievalMethod,
  type RetrievalMethodOption,
  type SearchMatch,
} from './searchApi'

const COLLAPSED_CONTENT_LENGTH = 600

function DocumentationSearch() {
  const [query, setQuery] = useState('')
  const [sources, setSources] = useState<Source[] | null>(null)
  const [selectedSourceIds, setSelectedSourceIds] = useState<Set<string>>(
    () => new Set(),
  )
  const [matches, setMatches] = useState<SearchMatch[] | null>(null)
  const [retrievalMethods, setRetrievalMethods] =
    useState<RetrievalMethodOption[] | null>(null)
  const [retrievalMethod, setRetrievalMethod] = useState<RetrievalMethod | ''>('')
  const [retrievalMethodsFailed, setRetrievalMethodsFailed] = useState(false)
  const [searchFailed, setSearchFailed] = useState(false)
  const [sourceLoadFailed, setSourceLoadFailed] = useState(false)
  const [searching, setSearching] = useState(false)
  const [expandedMatchIds, setExpandedMatchIds] = useState<Set<string>>(
    () => new Set(),
  )
  const sourceMenuRef = useRef<HTMLDetailsElement>(null)

  useEffect(() => {
    listSources()
      .then(setSources)
      .catch(() => {
        setSourceLoadFailed(true)
        setSources([])
      })
  }, [])

  useEffect(() => {
    listRetrievalMethods()
      .then((methods) => {
        setRetrievalMethods(methods)
        setRetrievalMethod(methods[0]?.retrievalMethod ?? '')
      })
      .catch(() => {
        setRetrievalMethodsFailed(true)
        setRetrievalMethods([])
      })
  }, [])

  useEffect(() => {
    function closeSourceMenu(event: PointerEvent) {
      const sourceMenu = sourceMenuRef.current

      if (
        sourceMenu?.open &&
        event.target instanceof Node &&
        !sourceMenu.contains(event.target)
      ) {
        sourceMenu.removeAttribute('open')
      }
    }

    document.addEventListener('pointerdown', closeSourceMenu)
    return () => document.removeEventListener('pointerdown', closeSourceMenu)
  }, [])

  async function submitSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const normalizedQuery = query.trim()
    if (!normalizedQuery || !retrievalMethod) {
      return
    }

    setSearching(true)
    setSearchFailed(false)
    setExpandedMatchIds(new Set())

    try {
      setMatches(
        await searchDocumentation(
          normalizedQuery,
          selectedSourceIds,
          retrievalMethod,
        ),
      )
    } catch {
      setSearchFailed(true)
    } finally {
      setSearching(false)
    }
  }

  function changeSourceSelection(sourceId: string, selected: boolean) {
    setSelectedSourceIds((current) => {
      const next = new Set(current)

      if (selected) {
        next.add(sourceId)
      } else {
        next.delete(sourceId)
      }

      return next
    })
  }

  function toggleMatch(matchId: string) {
    setExpandedMatchIds((current) => {
      const next = new Set(current)

      if (next.has(matchId)) {
        next.delete(matchId)
      } else {
        next.add(matchId)
      }

      return next
    })
  }

  const sourceSelectionSummary =
    selectedSourceIds.size === 0
      ? 'All Sources'
      : `${selectedSourceIds.size} ${
          selectedSourceIds.size === 1 ? 'Source' : 'Sources'
        } selected`

  return (
    <section
      id="documentation-search"
      className="search-management"
      aria-labelledby="documentation-search-title"
    >
      <div className="section-heading">
        <h2 id="documentation-search-title">Search documentation</h2>
      </div>

      <form className="search-form" onSubmit={submitSearch}>
        <div className="search-input-row">
          <input
            id="documentation-query"
            type="search"
            aria-label="Search query"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Search query"
            disabled={searching}
          />
          <button
            className="primary-button"
            type="submit"
            disabled={searching || !query.trim() || !retrievalMethod}
          >
            Search
          </button>
        </div>
        <div className="retrieval-method-selector">
          <label htmlFor="search-retrieval-method">Search method</label>
          <select
            id="search-retrieval-method"
            value={retrievalMethod}
            disabled={searching || retrievalMethods === null || retrievalMethodsFailed}
            onChange={(event) =>
              setRetrievalMethod(event.target.value as RetrievalMethod)
            }
          >
            {retrievalMethods === null ? (
              <option value="">Loading methods…</option>
            ) : retrievalMethods.length === 0 ? (
              <option value="">No search methods available</option>
            ) : (
              retrievalMethods.map((method) => (
                <option
                  key={method.retrievalMethod}
                  value={method.retrievalMethod}
                >
                  {method.displayName}
                </option>
              ))
            )}
          </select>
          {retrievalMethodsFailed ? (
            <p className="request-error" role="alert">
              Unable to load search methods. Try reloading the page.
            </p>
          ) : null}
        </div>
        <fieldset className="search-source-selector" disabled={searching}>
          <legend>Sources</legend>
          {sourceLoadFailed ? (
            <p className="request-error" role="alert">
              Unable to load Sources. The search will use the full corpus.
            </p>
          ) : sources === null ? (
            <p className="search-source-description" role="status">
              Loading Sources…
            </p>
          ) : sources.length === 0 ? (
            <p className="search-source-description">
              No Sources are available.
            </p>
          ) : (
            <details ref={sourceMenuRef} className="search-source-menu">
              <summary>{sourceSelectionSummary}</summary>
              <div className="search-source-menu-panel">
                <p className="search-source-description">
                  Select none to search all Sources.
                </p>
                <div className="search-source-options">
                  {sources.map((source) => (
                    <label key={source.id}>
                      <input
                        type="checkbox"
                        checked={selectedSourceIds.has(source.id)}
                        onChange={(event) =>
                          changeSourceSelection(source.id, event.target.checked)
                        }
                      />
                      {source.name}
                    </label>
                  ))}
                </div>
              </div>
            </details>
          )}
        </fieldset>
      </form>

      <div className="content-panel">
        {searchFailed ? (
          <p className="request-error" role="alert">
            Unable to search documentation. Try again.
          </p>
        ) : matches === null ? (
          <p className="empty-state">
            Enter a query to search synchronized documentation.
          </p>
        ) : matches.length === 0 ? (
          <p className="empty-state">No matching passages were found.</p>
        ) : (
          <>
            <div className="search-results-heading">
              <h3>Results</h3>
              <span className="search-result-count">
                {matches.length} {matches.length === 1 ? 'passage' : 'passages'}
              </span>
            </div>
            <ol className="search-results" aria-label="Search results">
              {matches.map((match) => {
                const canCollapse =
                  match.content.length > COLLAPSED_CONTENT_LENGTH
                const expanded = expandedMatchIds.has(match.chunkId)
                const displayedContent =
                  canCollapse && !expanded
                    ? `${match.content
                        .slice(0, COLLAPSED_CONTENT_LENGTH)
                        .trimEnd()}…`
                    : match.content

                return (
                  <li key={match.chunkId}>
                    <article>
                      <pre
                        id={`search-result-content-${match.chunkId}`}
                        className="search-result-content"
                      >
                        {displayedContent}
                      </pre>
                      {canCollapse ? (
                        <div className="search-result-expand-row">
                          <button
                            className="secondary-button search-result-expand"
                            type="button"
                            aria-expanded={expanded}
                            aria-controls={
                              `search-result-content-${match.chunkId}`
                            }
                            onClick={() => toggleMatch(match.chunkId)}
                          >
                            {expanded ? 'Collapse' : 'Expand'}
                          </button>
                        </div>
                      ) : null}
                      <footer className="search-result-citation">
                        <p>
                          <strong>Source:</strong>{' '}
                          {match.canonicalUrl ? (
                            <a
                              href={match.canonicalUrl}
                              target="_blank"
                              rel="noreferrer"
                            >
                              {match.sourceLocator}
                            </a>
                          ) : (
                            match.sourceLocator
                          )}
                        </p>
                        <p>{match.attribution}</p>
                      </footer>
                    </article>
                  </li>
                )
              })}
            </ol>
          </>
        )}
      </div>
    </section>
  )
}

export default DocumentationSearch
