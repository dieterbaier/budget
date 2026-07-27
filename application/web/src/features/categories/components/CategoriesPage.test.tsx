import { screen, fireEvent, waitFor, within } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { renderWithQuery } from '@/test/renderWithQuery'
import { CategoriesPage } from './CategoriesPage'

// The page is tested against stubbed `fetch` rather than a mocked api module:
// that keeps the feature's own wiring under test, and it is what CON-005 asks
// for anywhere outside the feature.

const groups = [{ name: 'House' }, { name: 'Car' }]
const categories = [
  { name: 'Groceries', group: 'House', pensionRelevant: true },
  { name: 'Mortgage', group: 'House', pensionRelevant: false },
]

function stubApi({ withGroups = groups, withCategories = categories } = {}) {
  const fetchMock = vi.fn(async (url: string, init?: RequestInit) => {
    if (init?.method === 'POST') return { ok: true, status: 201, json: async () => ({}) } as Response
    const body = url.includes('category-groups') ? withGroups : withCategories
    return { ok: true, status: 200, json: async () => body } as Response
  })
  vi.stubGlobal('fetch', fetchMock)
  return fetchMock
}

describe('CategoriesPage', () => {
  beforeEach(() => vi.clearAllMocks())
  afterEach(() => vi.unstubAllGlobals())

  it('lists categories with their group', async () => {
    stubApi()

    renderWithQuery(<CategoriesPage />)

    expect(await screen.findByText('Groceries')).toBeInTheDocument()
    // Scoped to the list: "House" also appears as an <option> in the group
    // picker, and matching that would prove nothing about the listing.
    expect(within(screen.getByRole('list')).getByText(/^House$/)).toBeInTheDocument()
  })

  // The flag decides whether the pension projection counts this spending, so
  // "not relevant" has to be visible rather than implied by its absence.
  it('marks a category that does not apply in retirement', async () => {
    stubApi()

    renderWithQuery(<CategoriesPage />)

    expect(await screen.findByText(/not pension relevant/)).toBeInTheDocument()
  })

  it('says what to do first when nothing exists yet', async () => {
    stubApi({ withGroups: [], withCategories: [] })

    renderWithQuery(<CategoriesPage />)

    // Offering a category form with no group to put it in would only produce a
    // rejection from the backend.
    expect(await screen.findByText(/Add a group before adding categories/)).toBeInTheDocument()
    // Anchored: /add category/ also matches the "Add category group" form.
    expect(screen.queryByRole('form', { name: /^add category$/i })).not.toBeInTheDocument()
  })

  it('creates a group', async () => {
    const fetchMock = stubApi()

    renderWithQuery(<CategoriesPage />)
    await screen.findByText('Groceries')

    fireEvent.change(screen.getByLabelText(/new group/i), { target: { value: 'Dog' } })
    fireEvent.click(screen.getByRole('button', { name: /add group/i }))

    await waitFor(() =>
      expect(fetchMock).toHaveBeenCalledWith(
        '/api/category-groups',
        expect.objectContaining({ method: 'POST', body: '{"name":"Dog"}' }),
      ),
    )
  })

  it('creates a category in the chosen group', async () => {
    const fetchMock = stubApi()

    renderWithQuery(<CategoriesPage />)
    await screen.findByText('Groceries')

    fireEvent.change(screen.getByLabelText(/new category/i), { target: { value: 'Fuel' } })
    fireEvent.change(screen.getByLabelText(/^group$/i), { target: { value: 'Car' } })
    fireEvent.click(screen.getByRole('button', { name: /add category/i }))

    await waitFor(() =>
      expect(fetchMock).toHaveBeenCalledWith(
        '/api/categories',
        expect.objectContaining({
          method: 'POST',
          body: '{"name":"Fuel","group":"Car","pensionRelevant":true}',
        }),
      ),
    )
  })

  it('shows the error when loading fails', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: false,
        status: 500,
        json: async () => ({ error: 'boom' }),
      } as Response),
    )

    renderWithQuery(<CategoriesPage />)

    expect(await screen.findByRole('alert')).toHaveTextContent('boom')
  })
})
