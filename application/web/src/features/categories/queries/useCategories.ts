import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createCategory,
  createCategoryGroup,
  getCategories,
  getCategoryGroups,
} from '../api/categories'

export const CATEGORIES_KEY = 'categories'
export const CATEGORY_GROUPS_KEY = 'category-groups'

export function useCategories() {
  return useQuery({ queryKey: [CATEGORIES_KEY], queryFn: getCategories })
}

export function useCategoryGroups() {
  return useQuery({ queryKey: [CATEGORY_GROUPS_KEY], queryFn: getCategoryGroups })
}

export function useCreateCategoryGroup() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: createCategoryGroup,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: [CATEGORY_GROUPS_KEY] }),
  })
}

// Creating a category changes both lists: the category list gains a row, and a
// group that was empty is no longer deletable.
export function useCreateCategory() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: createCategory,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [CATEGORIES_KEY] })
      queryClient.invalidateQueries({ queryKey: [CATEGORY_GROUPS_KEY] })
    },
  })
}
