import { z } from 'zod'
import { apiDelete, apiGet, apiPost, apiPut } from '@/shared/api/http'

export const categoryGroupSchema = z.object({ name: z.string() })

export const categorySchema = z.object({
  name: z.string(),
  group: z.string(),
  pensionRelevant: z.boolean(),
})

export type CategoryGroup = z.infer<typeof categoryGroupSchema>
export type Category = z.infer<typeof categorySchema>

export function getCategoryGroups(): Promise<CategoryGroup[]> {
  return apiGet(
    '/api/category-groups',
    z.array(categoryGroupSchema),
    'Failed to load category groups',
  )
}

export function getCategories(): Promise<Category[]> {
  return apiGet('/api/categories', z.array(categorySchema), 'Failed to load categories')
}

export function createCategoryGroup(name: string): Promise<void> {
  return apiPost('/api/category-groups', { name }, 'Failed to create the category group')
}

export function createCategory(category: {
  name: string
  group: string
  pensionRelevant: boolean
}): Promise<void> {
  return apiPost('/api/categories', category, 'Failed to create the category')
}

// The name addresses the resource through the query string rather than through a
// path segment. "Gesundheit / Arzt" is a name the owner keeps, and a
// percent-encoded slash in a path segment never reaches the backend -- Tomcat
// answers 400 before any handler runs (issue #82). `URLSearchParams` does the
// encoding, so no call site escapes anything by hand.
function addressedBy(collection: string, name: string): string {
  return `${collection}?${new URLSearchParams({ name })}`
}

// The name is the identity (ADR-009), so it addresses the resource and the body
// carries the new one. Renaming and regrouping are one request because they are
// one correction to the owner.
export function renameCategoryGroup(currentName: string, name: string): Promise<CategoryGroup> {
  return apiPut(
    addressedBy('/api/category-groups', currentName),
    { name },
    categoryGroupSchema,
    'Failed to rename the category group',
  )
}

export function updateCategory(
  currentName: string,
  category: { name: string; group: string; pensionRelevant: boolean },
): Promise<Category> {
  return apiPut(
    addressedBy('/api/categories', currentName),
    category,
    categorySchema,
    'Failed to update the category',
  )
}

export function deleteCategoryGroup(name: string): Promise<void> {
  return apiDelete(
    addressedBy('/api/category-groups', name),
    'Failed to delete the category group',
  )
}

export function deleteCategory(name: string): Promise<void> {
  return apiDelete(addressedBy('/api/categories', name), 'Failed to delete the category')
}
