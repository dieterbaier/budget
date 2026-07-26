import { describe, it, expect, vi, afterEach } from 'vitest'
import { getMonthlyExpenditure } from './monthlyExpenditure'

// The component test mocks this module, so without these the real request is
// never exercised: the URL it builds and the schema it parses against would both
// be unverified.

function respondWith(body: unknown, ok = true, status = 200) {
  const fetchMock = vi.fn().mockResolvedValue({ ok, status, json: async () => body } as Response)
  vi.stubGlobal('fetch', fetchMock)
  return fetchMock
}

const payload = {
  month: '2026-07',
  variableCosts: 900,
  fixedCostsMonthly: 100,
  total: 1000,
  averageIncome: 950,
  difference: 50,
  overspending: true,
}

describe('getMonthlyExpenditure', () => {
  afterEach(() => vi.unstubAllGlobals())

  it('requests the month and returns the parsed figures', async () => {
    const fetchMock = respondWith(payload)

    await expect(getMonthlyExpenditure('2026-07')).resolves.toEqual(payload)
    expect(fetchMock).toHaveBeenCalledWith('/api/monthly-expenditure?month=2026-07')
  })

  it('encodes the month rather than interpolating it raw', async () => {
    const fetchMock = respondWith(payload)

    await getMonthlyExpenditure('2026/07')

    expect(fetchMock).toHaveBeenCalledWith('/api/monthly-expenditure?month=2026%2F07')
  })

  it('rejects a payload that does not match the schema', async () => {
    respondWith({ ...payload, total: 'lots' })

    await expect(getMonthlyExpenditure('2026-07')).rejects.toThrow(/total/)
  })
})
