import { fireEvent, render, screen, within } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import PublisherManagement from './PublisherManagement'
import { createPublisher, listPublishers } from './publisherApi'

vi.mock('./publisherApi', () => ({
  createPublisher: vi.fn(),
  listPublishers: vi.fn(),
}))

beforeEach(() => {
  vi.mocked(createPublisher).mockReset()
  vi.mocked(listPublishers).mockReset()
})

async function openCreateDialog() {
  fireEvent.click(
    await screen.findByRole('button', { name: 'Create Publisher' }),
  )
  return screen.getByRole('dialog', { name: 'Create Publisher' })
}

describe('PublisherManagement', () => {
  it('shows a loading state while Publishers are being loaded', () => {
    vi.mocked(listPublishers).mockReturnValue(new Promise(() => {}))

    render(<PublisherManagement />)

    expect(screen.getByRole('status')).toHaveTextContent(
      'Loading Publishers…',
    )
  })

  it('shows an empty state when no Publishers are registered', async () => {
    vi.mocked(listPublishers).mockResolvedValue([])

    render(<PublisherManagement />)

    expect(
      await screen.findByText('No Publishers are registered yet.'),
    ).toBeInTheDocument()
  })

  it('shows Publishers in the order returned by the API', async () => {
    vi.mocked(listPublishers).mockResolvedValue([
      {
        id: '53f81218-3017-4852-a65f-e68e79813436',
        name: 'Alpha Documentation',
      },
      {
        id: 'c27f646d-2a09-4239-b86f-f169988b80f8',
        name: 'Zulu Documentation',
      },
    ])

    render(<PublisherManagement />)

    const items = await screen.findAllByRole('listitem')

    expect(items.map((item) => item.textContent)).toEqual([
      'Alpha Documentation',
      'Zulu Documentation',
    ])
  })

  it('shows a request-error state when Publishers cannot be loaded', async () => {
    vi.mocked(listPublishers).mockRejectedValue(new Error('Network failure'))

    render(<PublisherManagement />)

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Unable to load Publishers.',
    )
  })

  it('opens Publisher creation in a modal dialog', async () => {
    vi.mocked(listPublishers).mockResolvedValue([])

    render(<PublisherManagement />)

    expect(
      screen.queryByRole('textbox', { name: 'Publisher name' }),
    ).not.toBeInTheDocument()

    const dialog = await openCreateDialog()

    expect(
      within(dialog).getByRole('textbox', { name: 'Publisher name' }),
    ).toBeInTheDocument()
    expect(dialog).toHaveAttribute('aria-modal', 'true')
  })

  it('rejects a blank Publisher name before sending a request', async () => {
    vi.mocked(listPublishers).mockResolvedValue([])

    render(<PublisherManagement />)

    const dialog = await openCreateDialog()
    fireEvent.click(
      within(dialog).getByRole('button', { name: 'Create Publisher' }),
    )

    expect(createPublisher).not.toHaveBeenCalled()
    expect(screen.getByText('Publisher name is required.')).toBeInTheDocument()
    expect(
      within(dialog).getByRole('textbox', { name: 'Publisher name' }),
    ).toHaveAttribute('aria-invalid', 'true')
  })

  it('shows a submitting state while a Publisher is being created', async () => {
    vi.mocked(listPublishers).mockResolvedValue([])
    vi.mocked(createPublisher).mockReturnValue(new Promise(() => {}))

    render(<PublisherManagement />)

    const dialog = await openCreateDialog()
    const nameInput = within(dialog).getByRole('textbox', {
      name: 'Publisher name',
    })
    fireEvent.change(nameInput, { target: { value: 'GitHub Docs' } })
    fireEvent.click(
      within(dialog).getByRole('button', { name: 'Create Publisher' }),
    )

    expect(
      within(dialog).getByRole('button', { name: 'Creating…' }),
    ).toBeDisabled()
    expect(nameInput).toBeDisabled()
  })

  it('creates a Publisher and shows it in the list', async () => {
    vi.mocked(listPublishers).mockResolvedValue([])
    vi.mocked(createPublisher).mockResolvedValue({
      id: 'c27f646d-2a09-4239-b86f-f169988b80f8',
      name: 'GitHub Docs',
    })

    render(<PublisherManagement />)

    const dialog = await openCreateDialog()
    const nameInput = within(dialog).getByRole('textbox', {
      name: 'Publisher name',
    })
    fireEvent.change(nameInput, { target: { value: '  GitHub Docs  ' } })
    fireEvent.click(
      within(dialog).getByRole('button', { name: 'Create Publisher' }),
    )

    expect(await screen.findByText('GitHub Docs')).toBeInTheDocument()
    expect(createPublisher).toHaveBeenCalledWith({ name: 'GitHub Docs' })
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()

    const reopenedDialog = await openCreateDialog()
    expect(
      within(reopenedDialog).getByRole('textbox', { name: 'Publisher name' }),
    ).toHaveValue('')
  })

  it('shows a request-error state when a Publisher cannot be created', async () => {
    vi.mocked(listPublishers).mockResolvedValue([])
    vi.mocked(createPublisher).mockRejectedValue(new Error('Network failure'))

    render(<PublisherManagement />)

    const dialog = await openCreateDialog()
    const nameInput = within(dialog).getByRole('textbox', {
      name: 'Publisher name',
    })
    fireEvent.change(nameInput, { target: { value: 'GitHub Docs' } })
    fireEvent.click(
      within(dialog).getByRole('button', { name: 'Create Publisher' }),
    )

    expect(await within(dialog).findByRole('alert')).toHaveTextContent(
      'Unable to create Publisher. Try again.',
    )
    expect(nameInput).toHaveValue('GitHub Docs')
  })
})
