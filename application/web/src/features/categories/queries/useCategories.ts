import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createCategory,
  createCategoryGroup,
  deleteCategory,
  deleteCategoryGroup,
  getCategories,
  getCategoryGroups,
  renameCategoryGroup,
  updateCategory,
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

// Renaming a group changes the group shown against every category in it, so both
// lists are invalidated -- the same reason creating a category invalidates both.
export function useRenameCategoryGroup() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ currentName, name }: { currentName: string; name: string }) =>
      renameCategoryGroup(currentName, name),
    onSuccess: () => invalidateBoth(queryClient),
  })
}

export function useUpdateCategory() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({
      currentName,
      ...category
    }: {
      currentName: string
      name: string
      group: string
      pensionRelevant: boolean
    }) => updateCategory(currentName, category),
    onSuccess: () => invalidateBoth(queryClient),
  })
}

export function useDeleteCategoryGroup() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: deleteCategoryGroup,
    onSuccess: () => invalidateBoth(queryClient),
  })
}

export function useDeleteCategory() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: deleteCategory,
    onSuccess: () => invalidateBoth(queryClient),
  })
}

function invalidateBoth(queryClient: ReturnType<typeof useQueryClient>) {
  queryClient.invalidateQueries({ queryKey: [CATEGORIES_KEY] })
  queryClient.invalidateQueries({ queryKey: [CATEGORY_GROUPS_KEY] })
}
