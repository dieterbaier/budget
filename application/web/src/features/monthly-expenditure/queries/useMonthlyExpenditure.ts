import { useQuery } from '@tanstack/react-query'
import { getMonthlyExpenditure } from '../api/monthlyExpenditure'

// Exported through the feature's public API so that another feature can
// invalidate this data without inventing a matching string. The key is part of
// the contract; the query function behind it is not.
export const MONTHLY_EXPENDITURE_KEY = 'monthly-expenditure'

export function useMonthlyExpenditure(month: string) {
  return useQuery({
    queryKey: [MONTHLY_EXPENDITURE_KEY, month],
    queryFn: () => getMonthlyExpenditure(month),
  })
}
