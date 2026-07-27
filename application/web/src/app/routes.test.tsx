import { screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { renderWithQuery } from '@/test/renderWithQuery'
import { App } from './App'

// Routing is the reason React Router was adopted at all (ADR-014 deferred it
// until a second route existed), so the thing worth asserting is that the two
// routes render different things and that the links move between them.

const figures = {
  month: '2026-07',
  variableCosts: 900,
  fixedCostsMonthly: 100,
  total: 1000,
  averageIncome: 950,
  difference: 50,
  overspending: true,
}

function stubApi() {
  const fetchMock = vi.fn(async (url: string) => {
    if (url.includes('category-groups')) {
      return { ok: true, status: 200, json: async () => [{ name: 'House' }] } as Response
    }
    if (url.includes('/api/categories')) {
      return {
        ok: true,
        status: 200,
        json: async () => [{ name: 'Groceries', group: 'House', pensionRelevant: true }],
      } as Response
    }
    return { ok: true, status: 200, json: async () => figures } as Response
  })
  vi.stubGlobal('fetch', fetchMock)
  return fetchMock
}

describe('routes', () => {
  beforeEach(() => vi.clearAllMocks())
  afterEach(() => vi.unstubAllGlobals())

  it('shows the budget on the root route', async () => {
    stubApi()

    renderWithQuery(<App />)

    expect(await screen.findByRole('status')).toBeInTheDocument()
    expect(screen.getByRole('form', { name: /record transaction/i })).toBeInTheDocument()
  })

  it('shows the categories page on its own route', async () => {
    stubApi()

    renderWithQuery(<App />, { route: '/categories' })

    expect(await screen.findByRole('form', { name: /add category group/i })).toBeInTheDocument()
    // The budget view belongs to the other route and must not render here.
    expect(screen.queryByRole('form', { name: /record transaction/i })).not.toBeInTheDocument()
  })

  it('navigates between the two sections', async () => {
    stubApi()

    renderWithQuery(<App />)
    await screen.findByRole('status')

    fireEvent.click(screen.getByRole('link', { name: /categories/i }))

    expect(await screen.findByRole('form', { name: /add category group/i })).toBeInTheDocument()
  })
})
