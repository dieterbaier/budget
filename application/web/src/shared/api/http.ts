// The one place the client talks to HTTP. The Vite dev proxy forwards /api to
// the backend, so requests are same-origin in dev and in production alike.
//
// `fetch` resolves rather than rejects on a 4xx or 5xx, so without this helper
// every feature repeats the same `response.ok` check. The backend's
// ApiExceptionHandler sends `{ "error": "..." }` on a failure, which is the
// message worth showing; the caller's `whenFailed` text is the fallback for
// responses that carry no body.

export class ApiError extends Error {
  readonly status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

async function failOnError(response: Response, whenFailed: string): Promise<void> {
  if (response.ok) return
  const body = (await response.json().catch(() => null)) as { error?: string } | null
  throw new ApiError(body?.error ?? `${whenFailed} (${response.status})`, response.status)
}

export async function apiGet<T>(path: string, whenFailed: string): Promise<T> {
  const response = await fetch(path)
  await failOnError(response, whenFailed)
  return response.json() as Promise<T>
}

export async function apiPost(path: string, body: unknown, whenFailed: string): Promise<void> {
  const response = await fetch(path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  await failOnError(response, whenFailed)
}
