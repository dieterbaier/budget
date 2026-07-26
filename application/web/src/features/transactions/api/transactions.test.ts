import { describe, it, expect, vi, afterEach } from 'vitest'
import { recordTransaction } from './transactions'

// The form test mocks this module, so the request it actually sends -- method,
// path and body -- would otherwise never be asserted anywhere.

function respondWith(body: unknown, ok = true, status = 201) {
  const fetchMock = vi.fn().mockResolvedValue({ ok, status, json: async () => body } as Response)
  vi.stubGlobal('fetch', fetchMock)
  return fetchMock
}

const request = {
  date: '2026-07-15',
  amount: 42,
  category: 'Groceries',
  type: 'EXPENSE',
} as const

describe('recordTransaction', () => {
  afterEach(() => vi.unstubAllGlobals())

  it('posts the transaction as JSON', async () => {
    const fetchMock = respondWith({})

    await recordTransaction({ ...request })

    expect(fetchMock).toHaveBeenCalledWith('/api/transactions', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    })
  })

  it('surfaces the error the backend reports', async () => {
    respondWith({ error: 'Unknown category: Nope' }, false, 400)

    await expect(recordTransaction({ ...request, category: 'Nope' })).rejects.toThrow(
      'Unknown category: Nope',
    )
  })
})
