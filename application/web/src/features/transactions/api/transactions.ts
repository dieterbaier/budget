import { apiPost } from '@/shared/api/http'

export type TransactionType = 'EXPENSE' | 'INCOME' | 'TRANSFER'

export interface RecordTransactionRequest {
  date: string
  amount: number
  category: string
  type: TransactionType
}

export function recordTransaction(request: RecordTransactionRequest): Promise<void> {
  return apiPost('/api/transactions', request, 'Failed to record transaction')
}
