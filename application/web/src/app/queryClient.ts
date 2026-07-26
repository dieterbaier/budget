import { QueryClient } from '@tanstack/react-query'

// Single-user app talking to its own backend: a failed request is worth showing
// immediately rather than retrying three times behind a spinner, which is what
// QS-002 measures. Data is refetched on invalidation, not on a timer.
export function createQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        refetchOnWindowFocus: false,
      },
    },
  })
}
