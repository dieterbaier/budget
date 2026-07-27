import { screen, within, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { renderWithQuery } from '@/test/renderWithQuery'
import { CategoryPicker } from './CategoryPicker'

const categories = [
  { name: 'Groceries', group: 'House', pensionRelevant: true },
  { name: 'Fuel', group: 'Car', pensionRelevant: false },
  { name: 'Mortgage', group: 'House', pensionRelevant: false },
]

function stubApi(body: unknown, { ok = true, status = 200 } = {}) {
  vi.stubGlobal('fetch', vi.fn(async () => ({ ok, status, json: async () => body })))
}

function noop() {}

describe('CategoryPicker', () => {
  beforeEach(() => vi.clearAllMocks())
  afterEach(() => vi.unstubAllGlobals())

  it('offers every category, grouped by its group', async () => {
    stubApi(categories)

    renderWithQuery(<CategoryPicker value="" onChange={noop} />)

    await screen.findByRole('option', { name: 'Groceries' })
    const picker = screen.getByLabelText(/^category$/i)
    // The groups are the structure the owner navigates by, so the assertion is
    // about the grouping and not only about the names being present somewhere.
    const house = within(picker).getByRole('group', { name: 'House' })
    expect(within(house).getAllByRole('option').map((o) => o.textContent)).toEqual([
      'Groceries',
      'Mortgage',
    ])
    const car = within(picker).getByRole('group', { name: 'Car' })
    expect(within(car).getAllByRole('option').map((o) => o.textContent)).toEqual(['Fuel'])
  })

  it('says what to do first when there is nothing to pick', async () => {
    stubApi([])

    renderWithQuery(<CategoryPicker value="" onChange={noop} />)

    expect(await screen.findByRole('link', { name: /add one first/i })).toHaveAttribute(
      'href',
      '/categories',
    )
    expect(screen.queryByRole('combobox')).not.toBeInTheDocument()
  })

  it('shows the error instead of an empty picker when the catalogue cannot be read', async () => {
    stubApi({ message: 'Service unavailable' }, { ok: false, status: 503 })

    renderWithQuery(<CategoryPicker value="" onChange={noop} />)

    expect(await screen.findByRole('alert')).toBeInTheDocument()
    expect(screen.queryByRole('combobox')).not.toBeInTheDocument()
  })

  it('reports the picked category to its caller', async () => {
    stubApi(categories)
    const onChange = vi.fn()

    renderWithQuery(<CategoryPicker value="" onChange={onChange} />)

    await screen.findByRole('option', { name: 'Fuel' })
    fireEvent.change(screen.getByLabelText(/^category$/i), { target: { value: 'Fuel' } })

    expect(onChange).toHaveBeenCalledWith('Fuel')
  })
})
