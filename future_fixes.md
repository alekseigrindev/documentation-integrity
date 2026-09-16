# Future fixes

## Backend-local Source targets and validation

**User problem:** An operator configuring a `LOCAL_DIRECTORY` Source needs to
use either a readable directory or one readable Markdown file on the machine
running the backend. Invalid `file:` targets must be rejected when the Source
is saved rather than during synchronization.

**Required behavior:** `SourceService` validates readable backend-host `file:`
URLs during registration and update. `LocalDirectorySourceScanner` supports a
directory or one Markdown file and retains only runtime access handling for a
saved target that later changes or disappears. The browser form uses a manually
entered backend-local path; it must not call a Java desktop picker.

**Why deferred:** M6 has completed its cited lexical-search and evaluation
preparation outcome. Browser selection of a local directory or file requires a
separate upload workflow and remains recorded in `docs/FUTURE_CAPABILITIES.md`.
