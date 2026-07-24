import { useState } from 'react'
import { MonthlyExpenditureView } from './components/MonthlyExpenditureView'
import { RecordTransactionForm } from './components/RecordTransactionForm'

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
