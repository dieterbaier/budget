import { describe, it, expect, vi, afterEach } from 'vitest'
import {
  createCategory,
  createCategoryGroup,
  getCategories,
  getCategoryGroups,
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
})
