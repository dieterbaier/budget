import { useState } from 'react'
import { recordTransaction, type TransactionType } from '../api/transactions'

interface Props {
  onRecorded: () => void
}

const TYPES: TransactionType[] = ['EXPENSE', 'INCOME', 'TRANSFER']

export function RecordTransactionForm({ onRecorded }: Props) {
  const [date, setDate] = useState('')
  const [amount, setAmount] = useState('')
  const [category, setCategory] = useState('')
  const [type, setType] = useState<TransactionType>('EXPENSE')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await recordTransaction({ date, amount: Number(amount), category, type })
      setAmount('')
      setCategory('')
      onRecorded()
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Unknown error')
    } finally {
      setSubmitting(false)
    }
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
      <button type="submit" disabled={submitting}>
        {submitting ? 'Saving…' : 'Record'}
      </button>
      {error && (
        <p role="alert" className="error">
          {error}
        </p>
      )}
    </form>
  )
}
