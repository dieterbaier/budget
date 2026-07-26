import { screen, fireEvent, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, afterEach, beforeEach } from 'vitest'
import { renderWithQuery } from '@/test/renderWithQuery'
import { App } from './App'

// The shell is tested against the network rather than against the features'
// internals. Mocking a feature's api module would mean reaching through its
// barrel into its interior, which CON-005 forbids — and rightly: stubbing
// `fetch` exercises the real composition, so this test would catch a feature
// wiring itself up wrongly, which a mocked one would not.

const figures = {
  month: '2026-07',
  variableCosts: 900,
  fixedCostsMonthly: 100,
  total: 1000,
  averageIncome: 950,
  difference: 50,
  overspending: true,
}

function stubFetch() {
  const fetchMock = vi
    .fn()
    .mockResolvedValue({ ok: true, status: 200, json: async () => figures } as Response)
  vi.stubGlobal('fetch', fetchMock)
  return fetchMock
}

describe('App', () => {
  beforeEach(() => vi.clearAllMocks())
  afterEach(() => vi.unstubAllGlobals())

  it('composes both features', async () => {
    stubFetch()

    renderWithQuery(<App />)

    expect(await screen.findByRole('status')).toBeInTheDocument()
    expect(screen.getByRole('form', { name: /record transaction/i })).toBeInTheDocument()
  })

  // The month picker is the shell's only state, and it decides which month the
  // expenditure view asks for.
  it('reloads the figures for a newly picked month', async () => {
    const fetchMock = stubFetch()

    renderWithQuery(<App />)
    await screen.findByRole('status')

    // Anchored: an unanchored /month/i also matches "Current monthly
    // expenditure".
    fireEvent.change(screen.getByLabelText(/^month$/i), { target: { value: '2026-06' } })

    await waitFor(() =>
      expect(fetchMock).toHaveBeenCalledWith('/api/monthly-expenditure?month=2026-06'),
    )
  })
})
