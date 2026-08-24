# Documentation Integrity Frontend

The frontend foundation for Documentation Integrity. It is a minimal React and
TypeScript application that currently displays a greeting screen.

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

```bash
npm run dev
```

Open the local URL printed by Vite, normally `http://localhost:5173/`.

## Run the test

```bash
npm test
```

The test verifies that the greeting screen renders the Documentation Integrity
heading and welcome message.

## Create a production build

```bash
npm run build
```

The static production files are written to `dist/`.

## Current scope

This frontend foundation contains only a static greeting screen and its
rendering test. Publisher management, Source management, ingestion, retrieval,
and chat behavior belong to later milestones.