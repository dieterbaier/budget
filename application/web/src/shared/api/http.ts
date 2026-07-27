// The one place the client talks to HTTP. The Vite dev proxy forwards /api to
// the backend, so requests are same-origin in dev and in production alike.
//
// Three failures are handled here rather than in every feature. `fetch` resolves
// rather than rejects on a 4xx or 5xx, so without `failOnError` each caller
// repeats the same `response.ok` test. A body that is not JSON at all throws a
// bare SyntaxError naming neither the request nor the path. And a body that is
// valid JSON of the wrong shape is `unknown` until something looks at it, so it
// is parsed against the caller's schema rather than cast — a cast is a promise
// to the compiler, not a check of the data (ADR-016).

import { config, safeParse } from 'zod/v4/core'
import type { $ZodType, output } from 'zod/v4/core'
import { en } from 'zod/locales'

// Zod Mini deliberately bundles no locale and would otherwise report a bare
// "Invalid input". Loading English restores "expected number, received string",
// which is the difference between knowing a field is wrong and knowing how.
// Costs about 1 KB gzipped; `http.test.ts` asserts the rich form, so removing
// this line fails the build rather than quietly degrading every diagnostic.
config(en())

export class ApiError extends Error {
  readonly status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

/**
 * The response arrived, but it is not what this client was built against — a
 * backend change, a schema that was never right, or a body that is not JSON.
 * Distinct from ApiError so a caller can tell "the server said no" from "the
 * server said something I do not understand".
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

async function readJson(response: Response, path: string): Promise<unknown> {
  try {
    return await response.json()
  } catch {
    // A proxy error page, or an HTML 200 from a misrouted request, lands here.
    // Without this the caller sees a SyntaxError naming neither the path nor
    // the request.
    throw new ApiResponseError(path, 'the body is not valid JSON')
  }
}

function parseOrFail<S extends $ZodType>(schema: S, body: unknown, path: string): output<S> {
  const result = safeParse(schema, body)
  if (result.success) return result.data

  // Name the field and how it is wrong: "total: Invalid input: expected number,
  // received string" is actionable; a stack trace from a component is not.
  const detail = result.error.issues
    .map((issue) => `${issue.path.join('.') || '(root)'}: ${issue.message}`)
    .join('; ')
  throw new ApiResponseError(path, detail)
}

export async function apiGet<S extends $ZodType>(
  path: string,
  schema: S,
  whenFailed: string,
): Promise<output<S>> {
  const response = await fetch(path)
  await failOnError(response, whenFailed)
  return parseOrFail(schema, await readJson(response, path), path)
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

export async function apiPut<S extends $ZodType>(
  path: string,
  body: unknown,
  schema: S,
  whenFailed: string,
): Promise<output<S>> {
  const response = await fetch(path, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  await failOnError(response, whenFailed)
  return parseOrFail(schema, await readJson(response, path), path)
}

// 204 No Content, so there is nothing to read and nothing to parse. A failure
// still carries the backend's message -- refusing to delete master data that is
// still referenced is the case this exists to report (ADR-021).
export async function apiDelete(path: string, whenFailed: string): Promise<void> {
  const response = await fetch(path, { method: 'DELETE' })
  await failOnError(response, whenFailed)
}
