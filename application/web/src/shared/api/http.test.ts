import { describe, it, expect, vi, afterEach } from 'vitest'
import { z } from 'zod'
import { apiGet, ApiError, ApiResponseError } from './http'

const schema = z.object({ total: z.number(), overspending: z.boolean() })

function respondWith(body: unknown, ok = true, status = 200) {
  vi.stubGlobal(
    'fetch',
    vi.fn().mockResolvedValue({ ok, status, json: async () => body } as Response),
  )
}

describe('apiGet', () => {
  afterEach(() => vi.unstubAllGlobals())

  it('returns the body when it matches the schema', async () => {
    respondWith({ total: 1000, overspending: true })

    await expect(apiGet('/api/x', schema, 'failed')).resolves.toEqual({
      total: 1000,
      overspending: true,
    })
  })

  // The point of ADR-016: a backend change is caught where it arrives, naming
  // the field, instead of becoming NaN somewhere in a component.
  it('rejects a field of the wrong type and names it', async () => {
    respondWith({ total: 'lots', overspending: true })

    await expect(apiGet('/api/x', schema, 'failed')).rejects.toThrow(ApiResponseError)
    await expect(apiGet('/api/x', schema, 'failed')).rejects.toThrow(/total/)
  })

  it('rejects a renamed field rather than yielding undefined', async () => {
    respondWith({ sum: 1000, overspending: true })

    await expect(apiGet('/api/x', schema, 'failed')).rejects.toThrow(/total/)
  })

  it('reports the server error body ahead of any schema check', async () => {
    respondWith({ error: 'Unknown category: Nope' }, false, 400)

    await expect(apiGet('/api/x', schema, 'failed')).rejects.toThrow(ApiError)
    await expect(apiGet('/api/x', schema, 'failed')).rejects.toThrow('Unknown category: Nope')
  })
})
