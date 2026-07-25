import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { RecordTransactionForm } from './RecordTransactionForm'
import * as api from '../api/transactions'

vi.mock('../api/transactions', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../api/transactions')>()),
  recordTransaction: vi.fn(),
}))

describe('RecordTransactionForm', () => {
  beforeEach(() => vi.clearAllMocks())

  it('submits the entered transaction and notifies the parent', async () => {
    vi.mocked(api.recordTransaction).mockResolvedValue()
    const onRecorded = vi.fn()

    render(<RecordTransactionForm onRecorded={onRecorded} />)

    fireEvent.change(screen.getByLabelText(/date/i), { target: { value: '2026-07-15' } })
    fireEvent.change(screen.getByLabelText(/amount/i), { target: { value: '42' } })
    fireEvent.change(screen.getByLabelText(/category/i), { target: { value: 'Groceries' } })
    fireEvent.click(screen.getByRole('button', { name: /record/i }))

    await waitFor(() =>
      expect(api.recordTransaction).toHaveBeenCalledWith({
        date: '2026-07-15',
        amount: 42,
        category: 'Groceries',
        type: 'EXPENSE',
      }),
    )
    expect(onRecorded).toHaveBeenCalled()
  })

  it('shows the API error and does not notify the parent on failure', async () => {
    vi.mocked(api.recordTransaction).mockRejectedValue(new Error('Unknown category: Nope'))
    const onRecorded = vi.fn()

    render(<RecordTransactionForm onRecorded={onRecorded} />)

    fireEvent.change(screen.getByLabelText(/date/i), { target: { value: '2026-07-15' } })
    fireEvent.change(screen.getByLabelText(/amount/i), { target: { value: '10' } })
    fireEvent.change(screen.getByLabelText(/category/i), { target: { value: 'Nope' } })
    fireEvent.click(screen.getByRole('button', { name: /record/i }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Unknown category: Nope')
    expect(onRecorded).not.toHaveBeenCalled()
  })
})
