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
      <label className="field-label">
        Date
        <input
          className="field-control"
          type="date"
          value={date}
          onChange={(e) => setDate(e.target.value)}
          required
        />
      </label>
      <label className="field-label">
        Amount (EUR)
        <input
          className="field-control"
          type="number"
          step="0.01"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          required
        />
      </label>
      <label className="field-label">
        Category
        <input
          className="field-control"
          value={category}
          onChange={(e) => setCategory(e.target.value)}
          required
        />
      </label>
      <label className="field-label">
        Type
        <select
          className="field-control"
          value={type}
          onChange={(e) => setType(e.target.value as TransactionType)}
        >
          {TYPES.map((t) => (
            <option key={t} value={t}>
              {t}
            </option>
          ))}
        </select>
      </label>
      <button className="field-submit" type="submit" disabled={record.isPending}>
        {record.isPending ? 'Saving…' : 'Record'}
      </button>
      {record.error && (
        <p role="alert" className="mt-3 text-danger">
          {record.error.message}
        </p>
      )}
    </form>
  )
}
