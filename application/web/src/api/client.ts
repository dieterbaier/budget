// Typed client for the backend API. The Vite dev proxy forwards /api to the
// backend, so requests are same-origin in dev and prod.

export type TransactionType = 'EXPENSE' | 'INCOME' | 'TRANSFER'

export interface MonthlyExpenditure {
  month: string
  variableCosts: number
  fixedCostsMonthly: number
  total: number
  averageIncome: number
  difference: number
  overspending: boolean
}

export interface RecordTransactionRequest {
  date: string
  amount: number
  category: string
  type: TransactionType
}

export async function getMonthlyExpenditure(month: string): Promise<MonthlyExpenditure> {
  const response = await fetch(`/api/monthly-expenditure?month=${encodeURIComponent(month)}`)
  if (!response.ok) {
    throw new Error(`Failed to load monthly expenditure (${response.status})`)
  }
  return response.json() as Promise<MonthlyExpenditure>
}

export async function recordTransaction(request: RecordTransactionRequest): Promise<void> {
  const response = await fetch('/api/transactions', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })
  if (!response.ok) {
    const body = (await response.json().catch(() => null)) as { error?: string } | null
    throw new Error(body?.error ?? `Failed to record transaction (${response.status})`)
  }
}
