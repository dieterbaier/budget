import { useState } from 'react'
import { CategoryPicker } from '@/features/categories'
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
      {/*
        The category is chosen, not typed (issue #46). This feature knows only
        that the categories feature offers a picker; how categories are fetched
        and shaped stays behind that barrel (CON-005).
      */}
      <CategoryPicker value={category} onChange={setCategory} />
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
      {/*
        Without a category there is nothing to record, and when no category
        exists yet the picker renders a hint instead of a control -- so the
        button has to be the thing that holds the form shut, not `required`.
      */}
      <button className="field-submit" type="submit" disabled={record.isPending || !category}>
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
