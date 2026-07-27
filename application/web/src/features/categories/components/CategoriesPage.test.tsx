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
    expect(
      within(screen.getByRole('list', { name: /category list/i })).getByText(/^House$/),
    ).toBeInTheDocument()
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

  it('renames a group through its old name', async () => {
    const fetchMock = stubApi()

    renderWithQuery(<CategoriesPage />)
    await screen.findByText('Groceries')

    fireEvent.click(within(screen.getByRole('list', { name: /category groups/i }))
      .getAllByRole('button', { name: /rename/i })[0])
    fireEvent.change(screen.getByLabelText(/new name for House/i), { target: { value: 'Haus' } })
    fireEvent.click(screen.getByRole('button', { name: /^save$/i }))

    await waitFor(() =>
      expect(fetchMock).toHaveBeenCalledWith(
        '/api/category-groups/House',
        expect.objectContaining({ method: 'PUT', body: '{"name":"Haus"}' }),
      ),
    )
  })

  it('edits a category name, group and pension flag in one request', async () => {
    const fetchMock = stubApi()

    renderWithQuery(<CategoriesPage />)
    await screen.findByText('Groceries')

    fireEvent.click(within(screen.getByRole('list', { name: /category list/i }))
      .getAllByRole('button', { name: /^edit$/i })[0])
    fireEvent.change(screen.getByLabelText(/new name for Groceries/i), {
      target: { value: 'Food' },
    })
    fireEvent.change(screen.getByLabelText(/group for Groceries/i), { target: { value: 'Car' } })
    // Scoped to the edit row: the new-category form carries the same label.
    const editForm = screen.getByRole('form', { name: /edit category Groceries/i })
    fireEvent.click(within(editForm).getByLabelText(/still applies in retirement/i))
    fireEvent.click(within(editForm).getByRole('button', { name: /^save$/i }))

    await waitFor(() =>
      expect(fetchMock).toHaveBeenCalledWith(
        '/api/categories/Groceries',
        expect.objectContaining({
          method: 'PUT',
          body: '{"name":"Food","group":"Car","pensionRelevant":false}',
        }),
      ),
    )
  })

  it('deletes a category', async () => {
    const fetchMock = stubApi()

    renderWithQuery(<CategoriesPage />)
    await screen.findByText('Groceries')

    fireEvent.click(within(screen.getByRole('list', { name: /category list/i }))
      .getAllByRole('button', { name: /^delete$/i })[0])

    await waitFor(() =>
      expect(fetchMock).toHaveBeenCalledWith('/api/categories/Groceries', { method: 'DELETE' }),
    )
  })

  // The refusal is the interesting half of the delete rule: it names what still
  // references the category, which is what the owner has to move.
  it('shows why a deletion was refused', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async (url: string, init?: RequestInit) => {
        if (init?.method === 'DELETE') {
          return {
            ok: false,
            status: 409,
            json: async () => ({ error: '"Groceries" is still used by 42 transactions' }),
          } as Response
        }
        const body = url.includes('category-groups') ? groups : categories
        return { ok: true, status: 200, json: async () => body } as Response
      }),
    )

    renderWithQuery(<CategoriesPage />)
    await screen.findByText('Groceries')

    fireEvent.click(within(screen.getByRole('list', { name: /category list/i }))
      .getAllByRole('button', { name: /^delete$/i })[0])

    expect(await screen.findByRole('alert')).toHaveTextContent('still used by 42 transactions')
  })

  it('leaves the row unchanged when an edit is cancelled', async () => {
    const fetchMock = stubApi()

    renderWithQuery(<CategoriesPage />)
    await screen.findByText('Groceries')

    fireEvent.click(within(screen.getByRole('list', { name: /category list/i }))
      .getAllByRole('button', { name: /^edit$/i })[0])
    fireEvent.click(screen.getByRole('button', { name: /cancel/i }))

    expect(screen.queryByLabelText(/new name for Groceries/i)).not.toBeInTheDocument()
    expect(fetchMock).not.toHaveBeenCalledWith('/api/categories/Groceries', expect.anything())
  })

  // The cross-entity sequence this feature makes possible: rename a group, then
  // correct a category that was in it. The row stays mounted across the refetch
  // because its key is the category name, so a draft seeded once at mount would
  // submit the group that no longer exists.
  it('edits a category correctly after its group was renamed', async () => {
    let renamed = false
    const fetchMock = vi.fn(async (url: string, init?: RequestInit) => {
      if (init?.method === 'PUT' && url.includes('category-groups')) {
        renamed = true
        return { ok: true, status: 200, json: async () => ({ name: 'Home' }) } as Response
      }
      if (init?.method === 'PUT') {
        return {
          ok: true,
          status: 200,
          json: async () => ({ name: 'Groceries', group: 'Home', pensionRelevant: true }),
        } as Response
      }
      if (url.includes('category-groups')) {
        return {
          ok: true,
          status: 200,
          json: async () => (renamed ? [{ name: 'Home' }] : [{ name: 'House' }]),
        } as Response
      }
      return {
        ok: true,
        status: 200,
        json: async () => [
          { name: 'Groceries', group: renamed ? 'Home' : 'House', pensionRelevant: true },
        ],
      } as Response
    })
    vi.stubGlobal('fetch', fetchMock)

    renderWithQuery(<CategoriesPage />)
    await screen.findByText('Groceries')

    fireEvent.click(within(screen.getByRole('list', { name: /category groups/i }))
      .getByRole('button', { name: /rename/i }))
    fireEvent.change(screen.getByLabelText(/new name for House/i), { target: { value: 'Home' } })
    fireEvent.click(screen.getByRole('button', { name: /^save$/i }))

    // Wait for the refetch to land the renamed group on the *category* row --
    // that is the precondition for the bug, and an unscoped /Home/ would also
    // match the group row and the select's option.
    await waitFor(() =>
      expect(
        within(screen.getByRole('list', { name: /category list/i })).getByText(/Home/),
      ).toBeInTheDocument(),
    )

    fireEvent.click(within(screen.getByRole('list', { name: /category list/i }))
      .getByRole('button', { name: /^edit$/i }))
    const editForm = screen.getByRole('form', { name: /edit category Groceries/i })
    fireEvent.click(within(editForm).getByLabelText(/still applies in retirement/i))
    fireEvent.click(within(editForm).getByRole('button', { name: /^save$/i }))

    await waitFor(() =>
      expect(fetchMock).toHaveBeenCalledWith(
        '/api/categories/Groceries',
        expect.objectContaining({
          body: '{"name":"Groceries","group":"Home","pensionRelevant":false}',
        }),
      ),
    )
  })

  it('discards an abandoned edit when the row is reopened', async () => {
    stubApi()

    renderWithQuery(<CategoriesPage />)
    await screen.findByText('Groceries')

    const list = () => within(screen.getByRole('list', { name: /category list/i }))

    fireEvent.click(list().getAllByRole('button', { name: /^edit$/i })[0])
    fireEvent.change(screen.getByLabelText(/new name for Groceries/i), {
      target: { value: 'Abandoned' },
    })
    fireEvent.click(screen.getByRole('button', { name: /cancel/i }))

    fireEvent.click(list().getAllByRole('button', { name: /^edit$/i })[0])

    expect(screen.getByLabelText(/new name for Groceries/i)).toHaveValue('Groceries')
  })
})
