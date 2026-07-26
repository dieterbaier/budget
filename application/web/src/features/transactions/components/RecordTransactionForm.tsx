import { useState } from 'react'
import { useRecordTransaction } from '../queries/useRecordTransaction'
import type { TransactionType } from '../api/transactions'

const TYPES: TransactionType[] = ['EXPENSE', 'INCOME', 'TRANSFER']

export function RecordTransactionForm() {
  const [date, setDate] = useState('')
  const [amount, setAmount] = useState('')
  const [category, setCategory] = useState('')
  const [type, setType] = useState<TransactionType>('EXPENSE')

  const record = useRecordTransaction()

  function handleSubmit(event: React.FormEvent) {
    event.preventDefault()
    record.mutate(
      { date, amount: Number(amount), category, type },
      {
        onSuccess: () => {
          setAmount('')
          setCategory('')
        },
      },
    )
  }

  return (
    <form onSubmit={handleSubmit} aria-label="Record transaction">
      <label>
        Date
        <input type="date" value={date} onChange={(e) => setDate(e.target.value)} required />
      </label>
      <label>
        Amount (EUR)
        <input
          type="number"
          step="0.01"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          required
        />
      </label>
      <label>
        Category
        <input value={category} onChange={(e) => setCategory(e.target.value)} required />
      </label>
      <label>
        Type
        <select value={type} onChange={(e) => setType(e.target.value as TransactionType)}>
          {TYPES.map((t) => (
            <option key={t} value={t}>
              {t}
            </option>
          ))}
        </select>
      </label>
      <button type="submit" disabled={record.isPending}>
        {record.isPending ? 'Saving…' : 'Record'}
      </button>
      {record.error && (
        <p role="alert" className="error">
          {record.error.message}
        </p>
      )}
    </form>
  )
}
