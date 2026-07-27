import { useState } from 'react'
import type { Category } from '../api/categories'
import {
  useCategories,
  useCategoryGroups,
  useCreateCategory,
  useCreateCategoryGroup,
  useDeleteCategory,
  useDeleteCategoryGroup,
  useRenameCategoryGroup,
  useUpdateCategory,
} from '../queries/useCategories'

export function CategoriesPage() {
  const groups = useCategoryGroups()
  const categories = useCategories()

  if (groups.isPending || categories.isPending) return <p>Loading…</p>
  if (groups.error) return <p role="alert">{groups.error.message}</p>
  if (categories.error) return <p role="alert">{categories.error.message}</p>

  const groupNames = (groups.data ?? []).map((group) => group.name)

  return (
    <section aria-label="Categories">
      <NewGroupForm />

      <h3 className="mt-8 text-[1.1rem] font-semibold">Groups</h3>
      <GroupList names={groupNames} />

      <NewCategoryForm groups={groupNames} />

      <h3 className="mt-8 text-[1.1rem] font-semibold">Categories</h3>
      <CategoryList categories={categories.data ?? []} groups={groupNames} />
    </section>
  )
}

function GroupList({ names }: { names: string[] }) {
  if (names.length === 0) return <p className="text-muted">No groups yet.</p>

  return (
    <ul aria-label="Category groups" className="m-0 list-none p-0">
      {names.map((name) => (
        <GroupRow key={name} name={name} />
      ))}
    </ul>
  )
}

function GroupRow({ name }: { name: string }) {
  const [editing, setEditing] = useState(false)
  const [draft, setDraft] = useState(name)
  const rename = useRenameCategoryGroup()
  const remove = useDeleteCategoryGroup()

  // The message a refused deletion carries is the point of showing it: it names
  // how many categories are still in the group, which is what the owner has to
  // move (ADR-021).
  const failure = rename.error ?? remove.error

  return (
    <li className="border-b border-hairline py-2">
      {editing ? (
        <form
          aria-label={`Rename group ${name}`}
          className="flex items-center gap-2"
          onSubmit={(event) => {
            event.preventDefault()
            rename.mutate(
              { currentName: name, name: draft },
              { onSuccess: () => setEditing(false) },
            )
          }}
        >
          <input
            className="field-control flex-1"
            aria-label={`New name for ${name}`}
            value={draft}
            onChange={(event) => setDraft(event.target.value)}
            required
          />
          <button className="field-submit" type="submit" disabled={rename.isPending}>
            Save
          </button>
          <button type="button" className="text-sm text-muted" onClick={() => setEditing(false)}>
            Cancel
          </button>
        </form>
      ) : (
        <div className="flex items-center justify-between gap-2">
          <span>{name}</span>
          <span className="flex gap-3 text-sm">
            <button type="button" className="text-muted" onClick={() => setEditing(true)}>
              Rename
            </button>
            <button
              type="button"
              className="text-danger"
              onClick={() => remove.mutate(name)}
              disabled={remove.isPending}
            >
              Delete
            </button>
          </span>
        </div>
      )}
      {failure && (
        <p role="alert" className="mt-2 text-danger">
          {failure.message}
        </p>
      )}
    </li>
  )
}

function CategoryList({ categories, groups }: { categories: Category[]; groups: string[] }) {
  if (categories.length === 0) {
    return <p className="text-muted">No categories yet. Add a group, then a category.</p>
  }

  return (
    <ul aria-label="Category list" className="m-0 list-none p-0">
      {categories.map((category) => (
        <CategoryRow key={category.name} category={category} groups={groups} />
      ))}
    </ul>
  )
}

function CategoryRow({ category, groups }: { category: Category; groups: string[] }) {
  const [editing, setEditing] = useState(false)
  const [draft, setDraft] = useState(category)
  const update = useUpdateCategory()
  const remove = useDeleteCategory()

  const failure = update.error ?? remove.error

  return (
    <li className="border-b border-hairline py-2">
      {editing ? (
        <form
          aria-label={`Edit category ${category.name}`}
          className="flex flex-col gap-2"
          onSubmit={(event) => {
            event.preventDefault()
            update.mutate(
              { currentName: category.name, ...draft },
              { onSuccess: () => setEditing(false) },
            )
          }}
        >
          <input
            className="field-control"
            aria-label={`New name for ${category.name}`}
            value={draft.name}
            onChange={(event) => setDraft({ ...draft, name: event.target.value })}
            required
          />
          <select
            className="field-control"
            aria-label={`Group for ${category.name}`}
            value={draft.group}
            onChange={(event) => setDraft({ ...draft, group: event.target.value })}
          >
            {groups.map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
          <label className="text-sm">
            <input
              type="checkbox"
              checked={draft.pensionRelevant}
              onChange={(event) => setDraft({ ...draft, pensionRelevant: event.target.checked })}
            />{' '}
            Still applies in retirement
          </label>
          <span className="flex gap-2">
            <button className="field-submit" type="submit" disabled={update.isPending}>
              Save
            </button>
            <button type="button" className="text-sm text-muted" onClick={() => setEditing(false)}>
              Cancel
            </button>
          </span>
        </form>
      ) : (
        <div className="flex items-baseline justify-between gap-2">
          <span>{category.name}</span>
          <span className="flex items-baseline gap-3 text-xs">
            <span className="text-muted">
              {category.group}
              {category.pensionRelevant ? '' : ' · not pension relevant'}
            </span>
            <button type="button" className="text-sm text-muted" onClick={() => setEditing(true)}>
              Edit
            </button>
            <button
              type="button"
              className="text-sm text-danger"
              onClick={() => remove.mutate(category.name)}
              disabled={remove.isPending}
            >
              Delete
            </button>
          </span>
        </div>
      )}
      {failure && (
        <p role="alert" className="mt-2 text-danger">
          {failure.message}
        </p>
      )}
    </li>
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
