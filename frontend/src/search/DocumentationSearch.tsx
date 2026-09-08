import { type FormEvent, useState } from 'react'
import { searchDocumentation, type SearchMatch } from './searchApi'

function DocumentationSearch() {
  const [query, setQuery] = useState('')
  const [matches, setMatches] = useState<SearchMatch[] | null>(null)
  const [searchFailed, setSearchFailed] = useState(false)
  const [searching, setSearching] = useState(false)

  async function submitSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const normalizedQuery = query.trim()
    if (!normalizedQuery) {
      return
    }

    setSearching(true)
    setSearchFailed(false)

    try {
      setMatches(await searchDocumentation(normalizedQuery))
    } catch {
      setSearchFailed(true)
    } finally {
      setSearching(false)
    }
  }

  return (
    <section
      id="documentation-search"
      className="search-management"
      aria-labelledby="documentation-search-title"
    >
      <div className="section-heading">
        <div>
          <h2 id="documentation-search-title">Search documentation</h2>
          <p className="section-description">
            Find passages in synchronized documentation and inspect their
            citations.
          </p>
        </div>
      </div>

      <form className="search-form" onSubmit={submitSearch}>
        <label htmlFor="documentation-query">Search query</label>
        <div className="search-input-row">
          <input
            id="documentation-query"
            type="search"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="For example, write permission"
            disabled={searching}
          />
          <button
            className="primary-button"
            type="submit"
            disabled={searching || !query.trim()}
          >
            Search
          </button>
        </div>
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
          <ol className="search-results" aria-label="Search results">
            {matches.map((match) => (
              <li key={match.chunkId}>
                <article>
                  <pre className="search-result-content">{match.content}</pre>
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
            ))}
          </ol>
        )}
      </div>
    </section>
  )
}

export default DocumentationSearch
