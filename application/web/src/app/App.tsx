import { useState } from 'react'
import { MonthlyExpenditureView } from '@/features/monthly-expenditure'
import { RecordTransactionForm } from '@/features/transactions'

// The composition root. This is the only place allowed to know several features
// at once, and it reaches each of them through its public API like everyone
// else — breadth, not depth (CON-005). The router lands here when the second
// route exists (ADR-014).
export function App() {
  const [month, setMonth] = useState('2026-07')
  const [reloadToken, setReloadToken] = useState(0)

  return (
    <main>
      <h1>Budget</h1>

      <label className="month-picker">
        Month
        <input type="month" value={month} onChange={(e) => setMonth(e.target.value)} />
      </label>

      <h2>Current monthly expenditure</h2>
      <MonthlyExpenditureView month={month} reloadToken={reloadToken} />

      <h2>Record a transaction</h2>
      <RecordTransactionForm onRecorded={() => setReloadToken((token) => token + 1)} />
    </main>
  )
}
