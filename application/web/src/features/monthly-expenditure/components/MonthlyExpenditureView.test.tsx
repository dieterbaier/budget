import { screen } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderWithQuery } from '@/test/renderWithQuery'
import { MonthlyExpenditureView } from './MonthlyExpenditureView'
import * as api from '../api/monthlyExpenditure'

vi.mock('../api/monthlyExpenditure', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../api/monthlyExpenditure')>()),
  getMonthlyExpenditure: vi.fn(),
}))

describe('MonthlyExpenditureView', () => {
  beforeEach(() => vi.clearAllMocks())

  it('loads the month and shows the overspending signal', async () => {
    vi.mocked(api.getMonthlyExpenditure).mockResolvedValue({
      month: '2026-07',
      variableCosts: 900,
      fixedCostsMonthly: 100,
      total: 1000,
      averageIncome: 950,
      difference: 50,
      overspending: true,
    })

    renderWithQuery(<MonthlyExpenditureView month="2026-07" />)

    expect(await screen.findByRole('status')).toHaveTextContent(/Overspending/)
    expect(api.getMonthlyExpenditure).toHaveBeenCalledWith('2026-07')
    expect(screen.getByText('Total')).toBeInTheDocument()
  })

  it('shows an error message when loading fails', async () => {
    vi.mocked(api.getMonthlyExpenditure).mockRejectedValue(new Error('boom'))

    renderWithQuery(<MonthlyExpenditureView month="2026-07" />)

    expect(await screen.findByRole('alert')).toHaveTextContent('boom')
  })
})
