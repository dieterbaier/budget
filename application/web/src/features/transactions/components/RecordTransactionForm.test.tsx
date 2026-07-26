import { screen, fireEvent, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { MONTHLY_EXPENDITURE_KEY } from '@/features/monthly-expenditure'
import { renderWithQuery } from '@/test/renderWithQuery'
import { RecordTransactionForm } from './RecordTransactionForm'
import * as api from '../api/transactions'

vi.mock('../api/transactions', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../api/transactions')>()),
  recordTransaction: vi.fn(),
}))

function fillIn({ category = 'Groceries', amount = '42' } = {}) {
  fireEvent.change(screen.getByLabelText(/date/i), { target: { value: '2026-07-15' } })
  fireEvent.change(screen.getByLabelText(/amount/i), { target: { value: amount } })
  fireEvent.change(screen.getByLabelText(/category/i), { target: { value: category } })
  fireEvent.click(screen.getByRole('button', { name: /record/i }))
}

describe('RecordTransactionForm', () => {
  beforeEach(() => vi.clearAllMocks())

  it('submits the entered transaction', async () => {
    vi.mocked(api.recordTransaction).mockResolvedValue()

    renderWithQuery(<RecordTransactionForm />)
    fillIn()

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

    fillIn()

    await waitFor(() =>
      expect(invalidate).toHaveBeenCalledWith({ queryKey: [MONTHLY_EXPENDITURE_KEY] }),
    )
  })

  it('shows the API error when recording fails', async () => {
    vi.mocked(api.recordTransaction).mockRejectedValue(new Error('Unknown category: Nope'))

    renderWithQuery(<RecordTransactionForm />)
    fillIn({ category: 'Nope' })

    expect(await screen.findByRole('alert')).toHaveTextContent('Unknown category: Nope')
  })
})
