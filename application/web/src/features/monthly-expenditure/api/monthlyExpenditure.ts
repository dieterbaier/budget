import { z } from 'zod'
import { apiGet } from '@/shared/api/http'

// The schema is the single definition of this response: the type is inferred
// from it rather than written alongside it, so the two cannot disagree
// (ADR-016).
export const monthlyExpenditureSchema = z.object({
  month: z.string(),
  variableCosts: z.number(),
  fixedCostsMonthly: z.number(),
  total: z.number(),
  averageIncome: z.number(),
  difference: z.number(),
  overspending: z.boolean(),
})

export type MonthlyExpenditure = z.infer<typeof monthlyExpenditureSchema>

export function getMonthlyExpenditure(month: string): Promise<MonthlyExpenditure> {
  return apiGet(
    `/api/monthly-expenditure?month=${encodeURIComponent(month)}`,
    monthlyExpenditureSchema,
    'Failed to load monthly expenditure',
  )
}
