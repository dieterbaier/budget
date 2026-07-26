import { useState } from 'react'
import { MonthlyExpenditureView } from '@/features/monthly-expenditure'
import { RecordTransactionForm } from '@/features/transactions'

// The composition root. This is the only place allowed to know several features
// at once, and it reaches each of them through its public API like everyone
// else — breadth, not depth (CON-005). The router lands here when the second
// route exists (ADR-014).
//
// It no longer coordinates the refresh after a write: recording a transaction
// invalidates the month's figures through the query cache, so the two features
// meet in the cache rather than in a prop threaded through this component.
export function App() {
  const [month, setMonth] = useState('2026-07')

  return (
    <main className="max-w-[40rem] mx-auto px-4 pt-6 pb-16">
      <h1 className="text-2xl font-bold mb-4">Budget</h1>

      <label className="field-label">
        Month
        <input
          className="field-control"
          type="month"
          value={month}
          onChange={(e) => setMonth(e.target.value)}
        />
      </label>

      <h2 className="mt-8 text-[1.1rem] font-semibold">Current monthly expenditure</h2>
      <MonthlyExpenditureView month={month} />

      <h2 className="mt-8 text-[1.1rem] font-semibold">Record a transaction</h2>
      <RecordTransactionForm />
    </main>
  )
}
