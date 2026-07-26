import { describe, it, expect, vi, afterEach } from 'vitest'
import * as z from 'zod/mini'
import { apiGet, ApiError, ApiResponseError } from './http'

const schema = z.object({ total: z.number(), overspending: z.boolean() })

function respondWith(body: unknown, ok = true, status = 200) {
  vi.stubGlobal(
    'fetch',
    vi.fn().mockResolvedValue({ ok, status, json: async () => body } as Response),
  )
}

function respondWithNonJson(ok = true, status = 200) {
  vi.stubGlobal(
    'fetch',
    vi.fn().mockResolvedValue({
      ok,
      status,
      json: async () => {
        throw new SyntaxError('Unexpected token < in JSON at position 0')
      },
    } as unknown as Response),
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

  // Zod Mini bundles no locale and reports a bare "Invalid input" without one.
  // This asserts the configured locale is actually in effect, so dropping it
  // fails here rather than quietly degrading every diagnostic in the client.
  it('reports the expected and received types, not just the field', async () => {
    respondWith({ total: 'lots', overspending: true })

    await expect(apiGet('/api/x', schema, 'failed')).rejects.toThrow(
      /total: .*expected number, received string/,
    )
  })

  it('rejects a renamed field rather than yielding undefined', async () => {
    respondWith({ sum: 1000, overspending: true })

    await expect(apiGet('/api/x', schema, 'failed')).rejects.toThrow(
      /total: .*expected number, received undefined/,
    )
  })

  // A proxy error page, or a misrouted request returning HTML with a 200, lands
  // here. Without wrapping, the caller sees a bare SyntaxError naming neither
  // the path nor the request.
  it('reports a body that is not JSON at all, naming the path', async () => {
    respondWithNonJson()

    await expect(apiGet('/api/x', schema, 'failed')).rejects.toThrow(ApiResponseError)
    await expect(apiGet('/api/x', schema, 'failed')).rejects.toThrow(
      'Unexpected response from /api/x: the body is not valid JSON',
    )
  })

  it('reports the server error body ahead of any schema check', async () => {
    respondWith({ error: 'Unknown category: Nope' }, false, 400)

    await expect(apiGet('/api/x', schema, 'failed')).rejects.toThrow(ApiError)
    await expect(apiGet('/api/x', schema, 'failed')).rejects.toThrow('Unknown category: Nope')
  })
})
