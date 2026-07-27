import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { QueryBoundary, type QueryLike } from './QueryBoundary'

function query<T>(state: Partial<QueryLike<T>>): QueryLike<T> {
  return { isPending: false, error: null, data: undefined, ...state }
}

describe('QueryBoundary', () => {
  it('shows a placeholder while the query is pending', () => {
    render(<QueryBoundary query={query({ isPending: true })}>{() => <p>Data</p>}</QueryBoundary>)

    expect(screen.getByText('Loading…')).toBeInTheDocument()
    expect(screen.queryByText('Data')).not.toBeInTheDocument()
  })

  // The picker needs a field-shaped wait, not a paragraph, or the form jumps
  // when the catalogue arrives. That is the whole reason `pending` exists.
  it('lets the call site supply its own placeholder', () => {
    render(
      <QueryBoundary query={query({ isPending: true })} pending={<p>Own placeholder</p>}>
        {() => <p>Data</p>}
      </QueryBoundary>,
    )

    expect(screen.getByText('Own placeholder')).toBeInTheDocument()
    expect(screen.queryByText('Loading…')).not.toBeInTheDocument()
  })

  it('shows the error instead of the children when the query failed', () => {
    render(
      <QueryBoundary query={query({ error: new Error('Service unavailable') })}>
        {() => <p>Data</p>}
      </QueryBoundary>,
    )

    expect(screen.getByRole('alert')).toHaveTextContent('Service unavailable')
    expect(screen.queryByText('Data')).not.toBeInTheDocument()
  })

  it('hands the settled data to its children', () => {
    render(
      <QueryBoundary query={query({ data: ['Groceries', 'Fuel'] })}>
        {(names) => <p>{names.join(', ')}</p>}
      </QueryBoundary>,
    )

    expect(screen.getByText('Groceries, Fuel')).toBeInTheDocument()
  })

  // An error outranks stale data: showing figures next to a failed refresh
  // would present them as current when they are not.
  it('prefers the error over data it still holds', () => {
    render(
      <QueryBoundary query={query({ data: ['Groceries'], error: new Error('Refresh failed') })}>
        {(names) => <p>{names.join(', ')}</p>}
      </QueryBoundary>,
    )

    expect(screen.getByRole('alert')).toHaveTextContent('Refresh failed')
    expect(screen.queryByText('Groceries')).not.toBeInTheDocument()
  })

  it('renders nothing when a settled query somehow has no data', () => {
    const { container } = render(<QueryBoundary query={query({})}>{() => <p>Data</p>}</QueryBoundary>)

    expect(container).toBeEmptyDOMElement()
  })
})
