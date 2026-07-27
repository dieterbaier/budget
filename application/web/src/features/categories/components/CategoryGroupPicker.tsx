import { QueryBoundary } from '@/shared/ui/QueryBoundary'
import { useCategoryGroups } from '../queries/useCategories'

interface Props {
  value: string
  onChange: (groupName: string) => void
  /**
   * Names the control where no visible label wraps it. The two call sites label
   * differently -- the edit row is compact and names its fields through aria,
   * the add form uses a visible `field-label` -- so the picker renders the
   * control and leaves the labelling to whoever owns the surrounding form.
   */
  accessibleName?: string
}

/**
 * Chooses a category group, and fetches the groups itself.
 *
 * Deliberately *not* on the feature's barrel. Nothing outside `categories`
 * needs to pick a group yet; `CategoryPicker` is public because `transactions`
 * asked for it, and that is the bar. A group filter on the budget view would be
 * the moment to export this, not before.
 *
 * Fetching its own groups costs no second request -- the query key is shared, so
 * TanStack serves both this and the page from one -- and it is what lets the
 * page stop threading a `groups` array down through `CategoryList` into every
 * `CategoryRow`.
 */
export function CategoryGroupPicker({ value, onChange, accessibleName }: Props) {
  const groups = useCategoryGroups()

  return (
    <QueryBoundary
      query={groups}
      pending={
        <select className="field-control" aria-label={accessibleName} disabled>
          <option>Loading…</option>
        </select>
      }
    >
      {(loaded) => (
        <select
          className="field-control"
          aria-label={accessibleName}
          value={value}
          onChange={(event) => onChange(event.target.value)}
        >
          {loaded.map((group) => (
            <option key={group.name} value={group.name}>
              {group.name}
            </option>
          ))}
        </select>
      )}
    </QueryBoundary>
  )
}
