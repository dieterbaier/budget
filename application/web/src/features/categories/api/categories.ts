import { z } from 'zod'
import { apiGet, apiPost } from '@/shared/api/http'

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
