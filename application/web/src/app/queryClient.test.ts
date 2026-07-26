import { describe, it, expect } from 'vitest'
import { createQueryClient } from './queryClient'

// These two defaults are a decision, not an accident: no automatic refetch when
// the window regains focus, and no automatic retries. That means fewer requests
// and less radio time on the phone, and a failed request surfaces immediately
// rather than after three silent attempts. Asserting them here stops a later
// default from reinstating either without anyone noticing.
//
// Neither is polling: TanStack Query polls on `refetchInterval`, which is not
// configured here and defaults to off. Focus refetching is event-driven, and
// worth distinguishing — the two have different costs and different fixes.
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
