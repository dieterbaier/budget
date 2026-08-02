import { describe, it, expect, vi, afterEach } from 'vitest'
import {
  createCategory,
  createCategoryGroup,
  deleteCategory,
  deleteCategoryGroup,
  getCategories,
  getCategoryGroups,
  renameCategoryGroup,
  updateCategory,
} from './categories'

function respondWith(body: unknown, ok = true, status = 200) {
  const fetchMock = vi.fn().mockResolvedValue({ ok, status, json: async () => body } as Response)
  vi.stubGlobal('fetch', fetchMock)
  return fetchMock
}

describe('categories api', () => {
  afterEach(() => vi.unstubAllGlobals())

  it('lists groups', async () => {
    const fetchMock = respondWith([{ name: 'House' }])

    await expect(getCategoryGroups()).resolves.toEqual([{ name: 'House' }])
    expect(fetchMock).toHaveBeenCalledWith('/api/category-groups')
  })

  it('lists categories with their group and pension flag', async () => {
    respondWith([{ name: 'Groceries', group: 'House', pensionRelevant: true }])

    await expect(getCategories()).resolves.toEqual([
      { name: 'Groceries', group: 'House', pensionRelevant: true },
    ])
  })

  it('rejects a category list whose shape does not match', async () => {
    respondWith([{ name: 'Groceries', group: 'House' }])

    await expect(getCategories()).rejects.toThrow(/pensionRelevant/)
  })

  it('posts a new group', async () => {
    const fetchMock = respondWith({}, true, 201)

    await createCategoryGroup('House')

    expect(fetchMock).toHaveBeenCalledWith('/api/category-groups', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: '{"name":"House"}',
    })
  })

  it('posts a new category', async () => {
    const fetchMock = respondWith({}, true, 201)

    await createCategory({ name: 'Groceries', group: 'House', pensionRelevant: false })

    expect(fetchMock).toHaveBeenCalledWith('/api/categories', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: '{"name":"Groceries","group":"House","pensionRelevant":false}',
    })
  })

  // The backend refuses a duplicate with 409 and a message naming the clash;
  // the client must surface that text rather than a generic failure.
  it('surfaces the conflict the backend reports', async () => {
    respondWith({ error: 'A category named "Groceries" already exists' }, false, 409)

    await expect(createCategory({ name: 'Groceries', group: 'House', pensionRelevant: true }))
      .rejects.toThrow('A category named "Groceries" already exists')
  })

  it('renames a group through its old name', async () => {
    const fetchMock = respondWith({ name: 'House' })

    await expect(renameCategoryGroup('Huose', 'House')).resolves.toEqual({ name: 'House' })
    expect(fetchMock).toHaveBeenCalledWith('/api/category-groups?name=Huose', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: '{"name":"House"}',
    })
  })

  it('updates a category through its old name', async () => {
    const fetchMock = respondWith({ name: 'Groceries', group: 'House', pensionRelevant: false })

    await updateCategory('Grocries', { name: 'Groceries', group: 'House', pensionRelevant: false })

    expect(fetchMock).toHaveBeenCalledWith('/api/categories?name=Grocries', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: '{"name":"Groceries","group":"House","pensionRelevant":false}',
    })
  })

  // This used to assert a percent-encoded path segment, which was not enough:
  // Tomcat rejects an encoded slash in a path segment with a 400 before the
  // request reaches the application, so the name goes in the query string
  // (issue #82). The encoding was never the problem; where it was applied was.
  it('addresses a name containing a slash through the query string', async () => {
    const fetchMock = respondWith({ name: 'House' })

    await renameCategoryGroup('Haus/Hof', 'House')

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/category-groups?name=Haus%2FHof',
      expect.anything(),
    )
  })

  it('deletes a group and a category', async () => {
    const fetchMock = respondWith(undefined, true, 204)

    await deleteCategoryGroup('Empty')
    await deleteCategory('Mistake')

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/category-groups?name=Empty', {
      method: 'DELETE',
    })
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/categories?name=Mistake', {
      method: 'DELETE',
    })
  })

  // The refusal message names what still references the category, which is the
  // owner's next action.
  it('surfaces the refusal when the category is still in use', async () => {
    respondWith({ error: '"Groceries" is still used by 42 transactions' }, false, 409)

    await expect(deleteCategory('Groceries')).rejects.toThrow('still used by 42 transactions')
  })
})
