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

// The name is the identity (ADR-009), so it addresses the resource and the body
// carries the new one. Renaming and regrouping are one request because they are
// one correction to the owner.
export function renameCategoryGroup(currentName: string, name: string): Promise<CategoryGroup> {
  return apiPut(
    `/api/category-groups/${encodeURIComponent(currentName)}`,
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
    `/api/categories/${encodeURIComponent(currentName)}`,
    category,
    categorySchema,
    'Failed to update the category',
  )
}

export function deleteCategoryGroup(name: string): Promise<void> {
  return apiDelete(
    `/api/category-groups/${encodeURIComponent(name)}`,
    'Failed to delete the category group',
  )
}

export function deleteCategory(name: string): Promise<void> {
  return apiDelete(`/api/categories/${encodeURIComponent(name)}`, 'Failed to delete the category')
}
