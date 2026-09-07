# Documentation Integrity Frontend

The frontend for Documentation Integrity provides the completed M3 Publisher
management slice. An operator can load registered Publishers and create one
through a modal dialog. The browser uses Vite's local `/api` proxy to reach the
existing ingestion application.

## Prerequisites

- Node.js 22.12 or later. This project was verified with Node.js 22.22.2.
- npm, which is included with Node.js.

## Install

From the repository root:

```bash
cd frontend
npm install
```

`npm install` uses the committed `package-lock.json` to reproduce the
dependency set.

## Run locally

Start PostgreSQL from the repository root:

```bash
docker compose -f infra/compose.yaml up -d postgres
```

Start the ingestion application from `backend/`, supplying an authorized local
checkout path for its required ingestion configuration:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5444/documentation_integrity \
DATABASE_USERNAME=rag \
DATABASE_PASSWORD=rag \
DOCUMENTATION_CHECKOUT_ROOT=/absolute/path/to/authorized/checkout \
mvn -pl ingestion-service spring-boot:run
```

In another terminal, start the frontend from `frontend/`:

```bash
npm run dev
```

Open the local URL printed by Vite, normally `http://localhost:5173/`.

During local development, Vite accepts same-origin browser requests under
`/api` and proxies them to the ingestion application at
`http://localhost:8080`. The path is preserved, so a frontend request to
`/api/admin/publishers` reaches the existing ingestion-service endpoint without
requiring backend CORS configuration. This proxy is a local development
boundary; it is not production routing.

## Publisher API contract

The existing Publisher API is owned by `ingestion-service`. M3.1 records this
contract without changing it.

### List Publishers

```http
GET /api/admin/publishers
```

Successful response: `200 OK` with a JSON array. Each Publisher has the shape:

```json
{
  "id": "0f9304c5-c7d7-4387-9fef-f0d9dbaf20f0",
  "name": "GitHub Inc."
}
```

The list has stable ascending order by normalized Publisher name, with the
server-generated UUID as a deterministic tie-breaker.

### Create or find a Publisher

```http
POST /api/admin/publishers
Content-Type: application/json

{
  "name": "GitHub Inc."
}
```

The server removes leading and trailing whitespace from `name` before storage
and duplicate lookup.

| Condition | Status | Body |
| --- | --- | --- |
| A new normalized name is registered | `201 Created` | The created Publisher |
| The same normalized name already exists | `200 OK` | The existing Publisher |
| `name` is blank | `400 Bad Request` | Validation error |

Publisher creation is idempotent for the same normalized name: repeating the
request returns the existing Publisher and does not create another row.

## Source API contract

The existing Source API is owned by `ingestion-service`. M4 uses this contract
without changing it.

### List Sources

```http
GET /api/admin/sources
```

Successful response: `200 OK` with a JSON array in stable ascending order by
Source name and then ID. Each Source includes its `id`, `publisherId`,
`connectorType`, `sourceKey`, and `name`; description and URL/license metadata
are optional.

### Create or find a Source

```http
POST /api/admin/sources
Content-Type: application/json

{
  "publisherId": "c27f646d-2a09-4239-b86f-f169988b80f8",
  "connectorType": "github",
  "sourceKey": "github-docs",
  "name": "GitHub Docs"
}
```

The currently implemented connector type is `github`. `publisherId`,
`connectorType`, `sourceKey`, and `name` are required; description and
URL/license metadata are optional. The server trims `sourceKey` and treats it
as globally unique.

| Condition | Status | Body |
| --- | --- | --- |
| A new source key is registered | `201 Created` | The created Source |
| The same normalized source key already exists | `200 OK` | The existing Source |
| A required field is blank or missing | `400 Bad Request` | Validation error |
| The Publisher does not exist | `404 Not Found` | Not-found error |

Source creation is idempotent for the same normalized source key: repeating the
request returns the existing Source and does not create another row.

## Verify the local browser path

With PostgreSQL, the ingestion application, and Vite running as described
above, open the proxied endpoint in the browser:

```text
http://localhost:5173/api/admin/publishers
```

For reproducible terminal output, call the same Vite origin rather than port
8080 directly:

```bash
curl --fail-with-body --verbose http://localhost:5173/api/admin/publishers
```

Acceptance evidence is an HTTP `200` response containing the Publisher JSON
array. A successful response proves that the request reached Vite on port 5173,
was forwarded through the `/api` proxy, and was served by the real local
ingestion application on port 8080.

## Verify the frontend

```bash
npm test
npm run lint
npm run build
```

The tests cover Publisher and Source HTTP requests, visible list and creation
states, and management-view switching. On 2026-09-02, `npm test` passed with 5
test files and 34 tests;
`npm run lint` reported 0 warnings and 0 errors; and `npm run build` succeeded.

## Verified Publisher flow

With PostgreSQL, the ingestion application, and Vite running locally, an
operator can open `http://localhost:5173`, select **Create Publisher**, enter a
non-blank name, and submit. The request reaches the existing local backend
through Vite. On success, including an idempotent `200 OK` response, the modal
closes and the returned Publisher appears in the stable list.

The UI distinguishes loading, empty, request-error, validation-error, and
submitting states. It does not support editing or deleting Publishers.

## Verified Source flow

With PostgreSQL, the ingestion application, and Vite running locally, an
operator can open `http://localhost:5173`, select **Sources**, select
**Create Source**, choose a registered Publisher and the GitHub connector, then
enter a non-blank source key and name. On success, including an idempotent
`200 OK` response, the modal closes and the Source appears with its Publisher
and GitHub connector in the stable list.

The menu switches visibly between Sources and Publishers. The Source UI
distinguishes loading, no-Publisher, empty, request-error, validation-error,
and submitting states. It does not support Source editing, deletion, or
synchronization.

## Current scope

M3 and M4 are complete. M5 Source synchronization is active, but no
synchronization UI is implemented yet. Publisher or Source editing or deletion,
retrieval, diagnosis, and chat remain outside the implemented frontend scope.
