import { screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderWithQuery } from '@/test/renderWithQuery'
import { App } from './App'
import * as monthlyExpenditureApi from '@/features/monthly-expenditure/api/monthlyExpenditure'

vi.mock('@/features/monthly-expenditure/api/monthlyExpenditure', async (importOriginal) => ({
  ...(await importOriginal<typeof monthlyExpenditureApi>()),
  getMonthlyExpenditure: vi.fn(),
}))

const figures = {
  month: '2026-07',
  variableCosts: 900,
  fixedCostsMonthly: 100,
  total: 1000,
  averageIncome: 950,
  difference: 50,
  overspending: true,
}

describe('App', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(monthlyExpenditureApi.getMonthlyExpenditure).mockResolvedValue(figures)
  })

  it('composes both features', async () => {
    renderWithQuery(<App />)

    expect(await screen.findByRole('status')).toBeInTheDocument()
    expect(screen.getByRole('form', { name: /record transaction/i })).toBeInTheDocument()
  })

  // The month picker is the shell's only state, and it is what decides which
  // month the expenditure view asks for.
  it('reloads the figures for a newly picked month', async () => {
    renderWithQuery(<App />)
    await screen.findByRole('status')

    // Anchored: an unanchored /month/i also matches "Current monthly
    // expenditure".
    fireEvent.change(screen.getByLabelText(/^month$/i), { target: { value: '2026-06' } })

    expect(await screen.findByRole('status')).toBeInTheDocument()
    expect(monthlyExpenditureApi.getMonthlyExpenditure).toHaveBeenCalledWith('2026-06')
  })
})
