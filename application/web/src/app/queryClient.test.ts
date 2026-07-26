import { describe, it, expect } from 'vitest'
import { createQueryClient } from './queryClient'

// These two defaults are a decision, not an accident: no background polling and
// no automatic retries means fewer requests and less radio time on the phone,
// and a failed request surfaces immediately rather than after three silent
// attempts. Asserting them here stops a later default from reinstating polling
// without anyone noticing.
describe('createQueryClient', () => {
  it('does not retry failed queries', () => {
    const defaults = createQueryClient().getDefaultOptions()

    expect(defaults.queries?.retry).toBe(false)
  })

  it('does not refetch when the window regains focus', () => {
    const defaults = createQueryClient().getDefaultOptions()

    expect(defaults.queries?.refetchOnWindowFocus).toBe(false)
  })

  it('hands out an independent client each time', () => {
    expect(createQueryClient()).not.toBe(createQueryClient())
  })
})
