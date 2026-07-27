import { Link } from 'react-router-dom'
import { useCategories } from '../queries/useCategories'

interface Props {
  value: string
  onChange: (categoryName: string) => void
  label?: string
}

/**
 * Chooses a category. Exported from this feature's public API so that another
 * feature can offer the choice without knowing how categories are fetched or
 * shaped — which is what CON-005 asks for, and why `transactions` gets a
 * component rather than a hook and a list.
 *
 * The options are grouped, because "which category" is a question the owner
 * answers by first thinking of the group.
 */
export function CategoryPicker({ value, onChange, label = 'Category' }: Props) {
  const categories = useCategories()

  if (categories.isPending) {
    return (
      <label className="field-label">
        {label}
        <select className="field-control" disabled>
          <option>Loading…</option>
        </select>
      </label>
    )
  }

  if (categories.error) {
    return (
      <p role="alert" className="text-danger">
        {categories.error.message}
      </p>
    )
  }

  const available = categories.data ?? []

  // Nothing can be recorded before a category exists, and a picker with no
  // options is a dead end. Say where to go instead — the same choice the
  // categories page makes when there is no group to add a category to.
  if (available.length === 0) {
    return (
      <p className="text-muted">
        No categories yet. <Link to="/categories">Add one first</Link>.
      </p>
    )
  }

  const groups = [...new Set(available.map((category) => category.group))].sort()

  return (
    <label className="field-label">
      {label}
      <select
        className="field-control"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        required
      >
        <option value="">Choose a category…</option>
        {groups.map((group) => (
          <optgroup key={group} label={group}>
            {available
              .filter((category) => category.group === group)
              .map((category) => (
                <option key={category.name} value={category.name}>
                  {category.name}
                </option>
              ))}
          </optgroup>
        ))}
      </select>
    </label>
  )
}
