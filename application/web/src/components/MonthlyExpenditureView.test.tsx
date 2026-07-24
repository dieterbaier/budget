import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { MonthlyExpenditureView } from './MonthlyExpenditureView'
import * as client from '../api/client'

vi.mock('../api/client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../api/client')>()),
  getMonthlyExpenditure: vi.fn(),
}))

describe('MonthlyExpenditureView', () => {
  beforeEach(() => vi.clearAllMocks())

  it('loads the month and shows the overspending signal', async () => {
    vi.mocked(client.getMonthlyExpenditure).mockResolvedValue({
      month: '2026-07',
      variableCosts: 900,
      fixedCostsMonthly: 100,
      total: 1000,
      averageIncome: 950,
      difference: 50,
      overspending: true,
    })

    render(<MonthlyExpenditureView month="2026-07" reloadToken={0} />)

    expect(await screen.findByRole('status')).toHaveTextContent(/Overspending/)
    expect(client.getMonthlyExpenditure).toHaveBeenCalledWith('2026-07')
    expect(screen.getByText('Total')).toBeInTheDocument()
  })

  it('shows an error message when loading fails', async () => {
    vi.mocked(client.getMonthlyExpenditure).mockRejectedValue(new Error('boom'))

    render(<MonthlyExpenditureView month="2026-07" reloadToken={0} />)

    expect(await screen.findByRole('alert')).toHaveTextContent('boom')
  })
})
