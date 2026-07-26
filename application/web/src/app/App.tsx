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
    <main>
      <h1>Budget</h1>

      <label className="month-picker">
        Month
        <input type="month" value={month} onChange={(e) => setMonth(e.target.value)} />
      </label>

      <h2>Current monthly expenditure</h2>
      <MonthlyExpenditureView month={month} />

      <h2>Record a transaction</h2>
      <RecordTransactionForm />
    </main>
  )
}
