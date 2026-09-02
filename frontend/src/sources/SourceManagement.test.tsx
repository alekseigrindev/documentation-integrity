import { fireEvent, render, screen, within } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import SourceManagement from './SourceManagement'
import { createSource, listSources } from './sourceApi'
import { listPublishers } from '../publishers/publisherApi'

vi.mock('./sourceApi', () => ({
  createSource: vi.fn(),
  listSources: vi.fn(),
}))

vi.mock('../publishers/publisherApi', () => ({
  listPublishers: vi.fn(),
}))

const publisher = {
  id: 'c27f646d-2a09-4239-b86f-f169988b80f8',
  name: 'GitHub Docs',
}

const source = {
  id: '53f81218-3017-4852-a65f-e68e79813436',
  publisherId: publisher.id,
  connectorType: 'github' as const,
  sourceKey: 'github-docs',
  name: 'GitHub Docs',
}

beforeEach(() => {
  vi.mocked(createSource).mockReset()
  vi.mocked(listSources).mockReset()
  vi.mocked(listPublishers).mockReset()
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
    expect(screen.getByText('GitHub')).toBeInTheDocument()
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
    ).toHaveValue('github')
    expect(
      within(dialog).getByRole('option', { name: 'GitHub' }),
    ).toBeInTheDocument()
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
      connectorType: 'github',
      sourceKey: 'github-docs',
      name: 'GitHub Docs',
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
