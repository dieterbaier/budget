import type { ReactNode } from 'react'
import { ErrorMessage } from './ErrorMessage'
import { Loading } from './Loading'

/**
 * What this component needs of a query, and nothing more.
 *
 * Deliberately structural rather than `UseQueryResult` from TanStack. Every
 * query result satisfies it, so nothing is lost -- but `src/shared/ui` is
 * declared presentational in the boundaries config (it may not reach a feature
 * or `shared/api`), and importing the query library's types would walk the data
 * layer in through a door the lint rule does not watch, because an npm package
 * is not an element it knows about.
 */
export interface QueryLike<T> {
  isPending: boolean
  error: Error | null
  data: T | undefined
}

interface Props<T> {
  query: QueryLike<T>
  /** Overrides the default placeholder where the shape of the wait matters. */
  pending?: ReactNode
  children: (data: T) => ReactNode
}

/**
 * Renders one of a query's three states. Every reader was writing this branch
 * out by hand.
 *
 * The children are a function rather than nodes, which is the part that pays
 * for itself: past the two guards the data cannot be undefined, and saying so
 * through the signature removes the `?? []` and `if (!data) return null` that
 * call sites were writing to convince the compiler of what they already knew.
 */
export function QueryBoundary<T>({ query, pending, children }: Props<T>) {
  if (query.isPending) return <>{pending ?? <Loading />}</>
  if (query.error) return <ErrorMessage error={query.error} />
  // Not reachable through TanStack -- a settled query with no error has data --
  // but the type permits it, and returning nothing beats asserting otherwise.
  if (query.data === undefined) return null

  return <>{children(query.data)}</>
}
