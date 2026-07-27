import { useState } from 'react'
import {
  useCategories,
  useCategoryGroups,
  useCreateCategory,
  useCreateCategoryGroup,
} from '../queries/useCategories'

export function CategoriesPage() {
  const groups = useCategoryGroups()
  const categories = useCategories()

  if (groups.isPending || categories.isPending) return <p>Loading…</p>
  if (groups.error) return <p role="alert">{groups.error.message}</p>
  if (categories.error) return <p role="alert">{categories.error.message}</p>

  return (
    <section aria-label="Categories">
      <NewGroupForm />
      <NewCategoryForm groups={(groups.data ?? []).map((group) => group.name)} />

      <h3 className="mt-8 text-[1.1rem] font-semibold">Categories</h3>
      {categories.data && categories.data.length > 0 ? (
        <ul className="m-0 list-none p-0">
          {categories.data.map((category) => (
            <li
              key={category.name}
              className="flex items-baseline justify-between border-b border-hairline py-2"
            >
              <span>{category.name}</span>
              <span className="text-xs text-muted">
                {category.group}
                {category.pensionRelevant ? '' : ' · not pension relevant'}
              </span>
            </li>
          ))}
        </ul>
      ) : (
        <p className="text-muted">No categories yet. Add a group, then a category.</p>
      )}
    </section>
  )
}

function NewGroupForm() {
  const [name, setName] = useState('')
  const create = useCreateCategoryGroup()

  return (
    <form
      aria-label="Add category group"
      onSubmit={(event) => {
        event.preventDefault()
        create.mutate(name, { onSuccess: () => setName('') })
      }}
    >
      <label className="field-label">
        New group
        <input
          className="field-control"
          value={name}
          onChange={(event) => setName(event.target.value)}
          required
        />
      </label>
      <button className="field-submit" type="submit" disabled={create.isPending}>
        {create.isPending ? 'Adding…' : 'Add group'}
      </button>
      {create.error && (
        <p role="alert" className="mt-3 text-danger">
          {create.error.message}
        </p>
      )}
    </form>
  )
}

function NewCategoryForm({ groups }: { groups: string[] }) {
  const [name, setName] = useState('')
  const [group, setGroup] = useState('')
  const [pensionRelevant, setPensionRelevant] = useState(true)
  const create = useCreateCategory()

  // Without a group there is nothing to add a category to, and the backend would
  // reject it. Saying so beats offering a form that cannot succeed.
  if (groups.length === 0) {
    return <p className="mt-8 text-muted">Add a group before adding categories.</p>
  }

  return (
    <form
      aria-label="Add category"
      className="mt-8"
      onSubmit={(event) => {
        event.preventDefault()
        create.mutate(
          { name, group: group || groups[0], pensionRelevant },
          { onSuccess: () => setName('') },
        )
      }}
    >
      <label className="field-label">
        New category
        <input
          className="field-control"
          value={name}
          onChange={(event) => setName(event.target.value)}
          required
        />
      </label>
      <label className="field-label">
        Group
        <select
          className="field-control"
          value={group || groups[0]}
          onChange={(event) => setGroup(event.target.value)}
        >
          {groups.map((option) => (
            <option key={option} value={option}>
              {option}
            </option>
          ))}
        </select>
      </label>
      <label className="field-label">
        <span>
          <input
            type="checkbox"
            checked={pensionRelevant}
            onChange={(event) => setPensionRelevant(event.target.checked)}
          />{' '}
          Still applies in retirement
        </span>
      </label>
      <button className="field-submit" type="submit" disabled={create.isPending}>
        {create.isPending ? 'Adding…' : 'Add category'}
      </button>
      {create.error && (
        <p role="alert" className="mt-3 text-danger">
          {create.error.message}
        </p>
      )}
    </form>
  )
}
