import { screen, fireEvent, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { MONTHLY_EXPENDITURE_KEY } from '@/features/monthly-expenditure'
import { renderWithQuery } from '@/test/renderWithQuery'
import { RecordTransactionForm } from './RecordTransactionForm'
import * as api from '../api/transactions'

vi.mock('../api/transactions', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../api/transactions')>()),
  recordTransaction: vi.fn(),
}))

// The category picker comes from another feature, so this test may not reach
// into its api module to mock it (CON-005) -- it stubs the transport instead,
// the same way the routing test does.
function stubCategories(categories: { name: string; group: string }[]) {
  vi.stubGlobal(
    'fetch',
    vi.fn(async () => ({
      ok: true,
      status: 200,
      json: async () => categories.map((c) => ({ ...c, pensionRelevant: true })),
    })),
  )
}

// Waiting for the option rather than for the field: while the catalogue loads
// the picker already renders a Category field, so waiting on the label alone
// would fire the change into a select that has nothing to select yet.
async function pick(category: string) {
  await screen.findByRole('option', { name: category })
  fireEvent.change(screen.getByLabelText(/^category$/i), { target: { value: category } })
}

async function fillIn({ category = 'Groceries', amount = '42' } = {}) {
  fireEvent.change(screen.getByLabelText(/date/i), { target: { value: '2026-07-15' } })
  fireEvent.change(screen.getByLabelText(/amount/i), { target: { value: amount } })
  await pick(category)
  fireEvent.click(screen.getByRole('button', { name: /record/i }))
}

describe('RecordTransactionForm', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    stubCategories([{ name: 'Groceries', group: 'House' }])
  })
  afterEach(() => vi.unstubAllGlobals())

  it('submits the transaction with the category picked from the catalogue', async () => {
    vi.mocked(api.recordTransaction).mockResolvedValue()

    renderWithQuery(<RecordTransactionForm />)
    await fillIn()

    await waitFor(() =>
      expect(api.recordTransaction).toHaveBeenCalledWith({
        date: '2026-07-15',
        amount: 42,
        category: 'Groceries',
        type: 'EXPENSE',
      }),
    )
  })

  // This is what replaced the reloadToken counter the App used to thread
  // through both components: the write invalidates the reader's cache entry.
  it('invalidates the monthly expenditure so the figures refresh', async () => {
    vi.mocked(api.recordTransaction).mockResolvedValue()

    const { queryClient } = renderWithQuery(<RecordTransactionForm />)
    const invalidate = vi.spyOn(queryClient, 'invalidateQueries')

    await fillIn()

    await waitFor(() =>
      expect(invalidate).toHaveBeenCalledWith({ queryKey: [MONTHLY_EXPENDITURE_KEY] }),
    )
  })

  it('shows the API error when recording fails', async () => {
    vi.mocked(api.recordTransaction).mockRejectedValue(new Error('Recording failed'))

    renderWithQuery(<RecordTransactionForm />)
    await fillIn()

    expect(await screen.findByRole('alert')).toHaveTextContent('Recording failed')
  })

  // A free-text field let a typo through to the backend; a picker cannot, and
  // before any category exists there is nothing to pick, so the form has to
  // refuse rather than post an empty category.
  it('cannot be submitted before a category exists', async () => {
    stubCategories([])

    renderWithQuery(<RecordTransactionForm />)

    expect(await screen.findByText(/no categories yet/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /record/i })).toBeDisabled()
  })

  it('stays shut until a category is actually picked', async () => {
    renderWithQuery(<RecordTransactionForm />)
    await screen.findByRole('option', { name: 'Groceries' })

    expect(screen.getByRole('button', { name: /record/i })).toBeDisabled()

    await pick('Groceries')

    expect(screen.getByRole('button', { name: /record/i })).toBeEnabled()
  })
})
