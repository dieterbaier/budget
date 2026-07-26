// The one place the client talks to HTTP. The Vite dev proxy forwards /api to
// the backend, so requests are same-origin in dev and in production alike.
//
// Two things are checked here rather than in every feature. `fetch` resolves
// rather than rejects on a 4xx or 5xx, so without `failOnError` each caller
// repeats the same `response.ok` test. And a JSON body is `unknown` until
// something looks at it, so `parseOrFail` validates it against the caller's
// schema instead of casting — a cast is a promise to the compiler, not a check
// of the data (ADR-016).

import type { ZodType, output } from 'zod'

export class ApiError extends Error {
  readonly status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

/**
 * The response arrived, but it is not the shape this client was built against —
 * a backend change, or a schema that was never right. Distinct from ApiError so
 * a caller can tell "the server said no" from "the server said something I do
 * not understand".
 */
export class ApiResponseError extends Error {
  constructor(path: string, detail: string) {
    super(`Unexpected response from ${path}: ${detail}`)
    this.name = 'ApiResponseError'
  }
}

async function failOnError(response: Response, whenFailed: string): Promise<void> {
  if (response.ok) return
  const body = (await response.json().catch(() => null)) as { error?: string } | null
  throw new ApiError(body?.error ?? `${whenFailed} (${response.status})`, response.status)
}

function parseOrFail<S extends ZodType>(schema: S, body: unknown, path: string): output<S> {
  const result = schema.safeParse(body)
  if (result.success) return result.data

  // Name the field. "Unexpected response from /api/x: total: expected number,
  // received string" is actionable; a stack trace from a component is not.
  const detail = result.error.issues
    .map((issue) => `${issue.path.join('.') || '(root)'}: ${issue.message}`)
    .join('; ')
  throw new ApiResponseError(path, detail)
}

export async function apiGet<S extends ZodType>(
  path: string,
  schema: S,
  whenFailed: string,
): Promise<output<S>> {
  const response = await fetch(path)
  await failOnError(response, whenFailed)
  return parseOrFail(schema, await response.json(), path)
}

// The response body is deliberately not read. Nothing needs the created
// resource yet; when something does (issue #12 introduces transaction
// identity), it gets a schema like any other response.
export async function apiPost(path: string, body: unknown, whenFailed: string): Promise<void> {
  const response = await fetch(path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  await failOnError(response, whenFailed)
}
