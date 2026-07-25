import { apiGet } from '@/shared/api/http'

export interface MonthlyExpenditure {
  month: string
  variableCosts: number
  fixedCostsMonthly: number
  total: number
  averageIncome: number
  difference: number
  overspending: boolean
}

export function getMonthlyExpenditure(month: string): Promise<MonthlyExpenditure> {
  return apiGet<MonthlyExpenditure>(
    `/api/monthly-expenditure?month=${encodeURIComponent(month)}`,
    'Failed to load monthly expenditure',
  )
}
