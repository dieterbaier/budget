import { Link, Route, Routes, useLocation } from 'react-router-dom'
import { CategoriesPage } from '@/features/categories'
import { MonthlyExpenditureView } from '@/features/monthly-expenditure'
import { RecordTransactionForm } from '@/features/transactions'
import { useState } from 'react'

// ADR-014 deferred React Router until a second route existed. Issue #8 is that
// second route, so the router arrives here rather than in the shell: App stays
// the composition root and this holds the route table.

function BudgetPage() {
  const [month, setMonth] = useState('2026-07')

  return (
    <>
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
    </>
  )
}

function Navigation() {
  const { pathname } = useLocation()

  const linkClass = (path: string) =>
    pathname === path ? 'font-semibold underline' : 'text-muted no-underline'

  return (
    <nav aria-label="Sections" className="mb-6 flex gap-4 text-sm">
      <Link className={linkClass('/')} to="/">
        Budget
      </Link>
      <Link className={linkClass('/categories')} to="/categories">
        Categories
      </Link>
    </nav>
  )
}

export function AppRoutes() {
  return (
    <>
      <Navigation />
      <Routes>
        <Route path="/" element={<BudgetPage />} />
        <Route path="/categories" element={<CategoriesPage />} />
      </Routes>
    </>
  )
}
