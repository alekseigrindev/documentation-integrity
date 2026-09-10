import { fireEvent, render, screen, within } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import SourceManagement from './SourceManagement'
import {
  chooseLocalDirectory,
  createSource,
  listSources,
  updateSource,
} from './sourceApi'
import { listConnectors } from './connectorApi'
import {
  latestIngestionRun,
  synchronizeSource,
} from '../ingestion-runs/ingestionRunApi'
import { listPublishers } from '../publishers/publisherApi'

vi.mock('./sourceApi', () => ({
  chooseLocalDirectory: vi.fn(),
  createSource: vi.fn(),
  listSources: vi.fn(),
  updateSource: vi.fn(),
}))

vi.mock('../publishers/publisherApi', () => ({
  listPublishers: vi.fn(),
}))

vi.mock('./connectorApi', () => ({
  listConnectors: vi.fn(),
}))

vi.mock('../ingestion-runs/ingestionRunApi', () => ({
  latestIngestionRun: vi.fn(),
  synchronizeSource: vi.fn(),
}))

const publisher = {
  id: 'c27f646d-2a09-4239-b86f-f169988b80f8',
  name: 'GitHub Docs',
}

const connector = {
  type: 'local-directory',
  description: 'Reads supported documents from an allowed local directory.',
}

const source = {
  id: '53f81218-3017-4852-a65f-e68e79813436',
  publisherId: publisher.id,
  connectorType: connector.type,
  sourceKey: 'github-docs',
  name: 'GitHub Docs',
  sourceUrl: 'file:///Users/alekseigrindev/Documents/github-docs',
}

beforeEach(() => {
  vi.mocked(chooseLocalDirectory).mockReset()
  vi.mocked(createSource).mockReset()
  vi.mocked(listSources).mockReset()
  vi.mocked(updateSource).mockReset()
  vi.mocked(listPublishers).mockReset()
  vi.mocked(listConnectors).mockReset()
  vi.mocked(latestIngestionRun).mockReset()
  vi.mocked(synchronizeSource).mockReset()
  vi.mocked(listConnectors).mockResolvedValue([connector])
})

async function openCreateDialog() {
  fireEvent.click(await screen.findByRole('button', { name: 'Create Source' }))
  return screen.getByRole('dialog', { name: 'Create Source' })
}

function setValidValues(dialog: HTMLElement) {
  fireEvent.change(within(dialog).getByRole('combobox', { name: 'Publisher' }), {
    target: { value: publisher.id },
  })
  fireEvent.change(within(dialog).getByRole('textbox', { name: 'Source key' }), {
    target: { value: 'github-docs' },
  })
  fireEvent.change(within(dialog).getByRole('textbox', { name: 'Source name' }), {
    target: { value: 'GitHub Docs' },
  })
  fireEvent.change(
    within(dialog).getByRole('textbox', { name: 'Local directory path' }),
    {
      target: { value: '/Users/alekseigrindev/Documents/github-docs' },
    },
  )
}

describe('SourceManagement', () => {
  it('shows a loading state while Sources are being loaded', () => {
    vi.mocked(listPublishers).mockReturnValue(new Promise(() => {}))
    vi.mocked(listSources).mockResolvedValue([])

    render(<SourceManagement />)

    expect(screen.getByRole('status')).toHaveTextContent('Loading Sources…')
  })

  it('asks the operator to create a Publisher when none are available', async () => {
    vi.mocked(listPublishers).mockResolvedValue([])
    vi.mocked(listSources).mockResolvedValue([])

    render(<SourceManagement />)

    expect(
      await screen.findByText('Create a Publisher before adding a Source.'),
    ).toBeInTheDocument()
    expect(
      screen.queryByRole('button', { name: 'Create Source' }),
    ).not.toBeInTheDocument()
  })

  it('shows an empty state when no Sources are registered', async () => {
    vi.mocked(listPublishers).mockResolvedValue([publisher])
    vi.mocked(listSources).mockResolvedValue([])

    render(<SourceManagement />)

    expect(
      await screen.findByText('No Sources are registered yet.'),
    ).toBeInTheDocument()
  })

  it('shows each Source with its Publisher and connector type', async () => {
    vi.mocked(listPublishers).mockResolvedValue([publisher])
    vi.mocked(listSources).mockResolvedValue([source])

    render(<SourceManagement />)

    expect(await screen.findByText('GitHub Docs')).toBeInTheDocument()
    expect(screen.getByText(/Publisher: GitHub Docs/)).toBeInTheDocument()
    expect(screen.getByText('local-directory')).toBeInTheDocument()
  })

  it('shows a request-error state when Source management cannot be loaded', async () => {
    vi.mocked(listPublishers).mockResolvedValue([publisher])
    vi.mocked(listSources).mockRejectedValue(new Error('Network failure'))

    render(<SourceManagement />)

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Unable to load Source management.',
    )
  })

  it('opens Source creation in a modal dialog', async () => {
    vi.mocked(listPublishers).mockResolvedValue([publisher])
    vi.mocked(listSources).mockResolvedValue([])

    render(<SourceManagement />)

    const dialog = await openCreateDialog()

    expect(
      within(dialog).getByRole('combobox', { name: 'Publisher' }),
    ).toBeInTheDocument()
    expect(
      within(dialog).getByRole('combobox', { name: 'Connector' }),
    ).toHaveValue('local-directory')
    expect(
      within(dialog).getByRole('option', { name: 'local-directory' }),
    ).toBeInTheDocument()
  })

  it('generates a Source key from a new Source name and preserves a manual key', async () => {
    vi.mocked(listPublishers).mockResolvedValue([publisher])
    vi.mocked(listSources).mockResolvedValue([])

    render(<SourceManagement />)

    const dialog = await openCreateDialog()
    const sourceName = within(dialog).getByRole('textbox', {
      name: 'Source name',
    })
    const sourceKey = within(dialog).getByRole('textbox', {
      name: 'Source key',
    })

    fireEvent.change(sourceName, { target: { value: 'GitHub Docs' } })
    expect(sourceKey).toHaveValue('github-docs')

    fireEvent.change(sourceKey, { target: { value: 'github-actions-docs' } })
    fireEvent.change(sourceName, { target: { value: 'GitHub Actions Docs' } })
    expect(sourceKey).toHaveValue('github-actions-docs')
  })

  it('does not replace an existing Source key when its name changes', async () => {
    vi.mocked(listPublishers).mockResolvedValue([publisher])
    vi.mocked(listSources).mockResolvedValue([source])

    render(<SourceManagement />)

    fireEvent.click(await screen.findByRole('button', { name: 'Edit' }))
    const dialog = screen.getByRole('dialog', { name: 'Edit Source' })

    fireEvent.change(
      within(dialog).getByRole('textbox', { name: 'Source name' }),
      { target: { value: 'GitHub Actions Docs' } },
    )

    expect(
      within(dialog).getByRole('textbox', { name: 'Source key' }),
    ).toHaveValue('github-docs')
  })

  it('rejects blank Source fields before sending a request', async () => {
    vi.mocked(listPublishers).mockResolvedValue([publisher])
    vi.mocked(listSources).mockResolvedValue([])

    render(<SourceManagement />)

    const dialog = await openCreateDialog()
    fireEvent.click(within(dialog).getByRole('button', { name: 'Create Source' }))

    expect(createSource).not.toHaveBeenCalled()
    expect(within(dialog).getByText('Select a Publisher.')).toBeInTheDocument()
    expect(within(dialog).getByText('Source key is required.')).toBeInTheDocument()
    expect(within(dialog).getByText('Source name is required.')).toBeInTheDocument()
  })

  it('shows a submitting state while a Source is being created', async () => {
    vi.mocked(listPublishers).mockResolvedValue([publisher])
    vi.mocked(listSources).mockResolvedValue([])
    vi.mocked(createSource).mockReturnValue(new Promise(() => {}))

    render(<SourceManagement />)

    const dialog = await openCreateDialog()
    setValidValues(dialog)
    fireEvent.click(within(dialog).getByRole('button', { name: 'Create Source' }))

    expect(
      within(dialog).getByRole('button', { name: 'Creating…' }),
    ).toBeDisabled()
    expect(
      within(dialog).getByRole('combobox', { name: 'Publisher' }),
    ).toBeDisabled()
  })

  it('creates a Source and shows it with the selected Publisher', async () => {
    vi.mocked(listPublishers).mockResolvedValue([publisher])
    vi.mocked(listSources).mockResolvedValue([])
    vi.mocked(createSource).mockResolvedValue(source)

    render(<SourceManagement />)

    const dialog = await openCreateDialog()
    setValidValues(dialog)
    fireEvent.click(within(dialog).getByRole('button', { name: 'Create Source' }))

    expect(
      await screen.findByText(/Publisher: GitHub Docs/),
    ).toBeInTheDocument()
    expect(createSource).toHaveBeenCalledWith({
      publisherId: publisher.id,
      connectorType: 'local-directory',
      sourceKey: 'github-docs',
      name: 'GitHub Docs',
      sourceUrl: 'file:///Users/alekseigrindev/Documents/github-docs',
    })
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('shows a request-error state when a Source cannot be created', async () => {
    vi.mocked(listPublishers).mockResolvedValue([publisher])
    vi.mocked(listSources).mockResolvedValue([])
    vi.mocked(createSource).mockRejectedValue(new Error('Network failure'))

    render(<SourceManagement />)

    const dialog = await openCreateDialog()
    setValidValues(dialog)
    fireEvent.click(within(dialog).getByRole('button', { name: 'Create Source' }))

    expect(await within(dialog).findByRole('alert')).toHaveTextContent(
      'Unable to create Source. Try again.',
    )
    expect(
      within(dialog).getByRole('textbox', { name: 'Source name' }),
    ).toHaveValue('GitHub Docs')
  })
})
