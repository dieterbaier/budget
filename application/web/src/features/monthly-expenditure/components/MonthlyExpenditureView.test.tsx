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

  // The green path had no test at all: every case asserted overspending, so the
  // "within income" wording and its sign flip were unverified.
  it('shows how much is left when spending is within income', async () => {
    vi.mocked(api.getMonthlyExpenditure).mockResolvedValue({
      month: '2026-07',
      variableCosts: 500,
      fixedCostsMonthly: 100,
      total: 600,
      averageIncome: 950,
      difference: -350,
      overspending: false,
    })

    renderWithQuery(<MonthlyExpenditureView month="2026-07" />)

    // The component negates the difference, so a negative difference must read
    // as a positive amount remaining.
    expect(await screen.findByRole('status')).toHaveTextContent(/Within income by/)
    expect(screen.getByRole('status')).not.toHaveTextContent('-')
  })

  it('shows an error message when loading fails', async () => {
    vi.mocked(api.getMonthlyExpenditure).mockRejectedValue(new Error('boom'))

    renderWithQuery(<MonthlyExpenditureView month="2026-07" />)

    expect(await screen.findByRole('alert')).toHaveTextContent('boom')
  })
})
