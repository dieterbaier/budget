import { useMutation, useQueryClient } from '@tanstack/react-query'
import { MONTHLY_EXPENDITURE_KEY } from '@/features/monthly-expenditure'
import { recordTransaction, type RecordTransactionRequest } from '../api/transactions'

// Recording a transaction changes the month's figures, so this mutation
// invalidates them. The key is imported from the owning feature's public API
// rather than repeated as a literal: a magic string would be a dependency that
// no lint rule could see, and renaming the key elsewhere would silently stop
// the refresh instead of failing the build.
export function useRecordTransaction() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: RecordTransactionRequest) => recordTransaction(request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: [MONTHLY_EXPENDITURE_KEY] }),
  })
}
