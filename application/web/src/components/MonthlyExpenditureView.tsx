import { useEffect, useState } from 'react'
import { getMonthlyExpenditure, type MonthlyExpenditure } from '../api/client'
import { formatEur } from '../format'

interface Props {
  month: string
  reloadToken: number
}

export function MonthlyExpenditureView({ month, reloadToken }: Props) {
  const [data, setData] = useState<MonthlyExpenditure | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    let active = true
    setLoading(true)
    setError(null)
    getMonthlyExpenditure(month)
      .then((result) => {
        if (active) setData(result)
      })
      .catch((err: unknown) => {
        if (active) setError(err instanceof Error ? err.message : 'Unknown error')
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [month, reloadToken])

  if (loading && !data) return <p>Loading…</p>
  if (error) return <p role="alert">{error}</p>
  if (!data) return null

  return (
    <section aria-label="Current monthly expenditure">
      <dl className="figures">
        <div>
          <dt>Variable costs</dt>
          <dd>{formatEur(data.variableCosts)}</dd>
        </div>
        <div>
          <dt>Fixed costs (monthly)</dt>
          <dd>{formatEur(data.fixedCostsMonthly)}</dd>
        </div>
        <div>
          <dt>Total</dt>
          <dd>{formatEur(data.total)}</dd>
        </div>
        <div>
          <dt>Average income</dt>
          <dd>{formatEur(data.averageIncome)}</dd>
        </div>
      </dl>
      <p className={data.overspending ? 'signal over' : 'signal ok'} role="status">
        {data.overspending
          ? `Overspending by ${formatEur(data.difference)}`
          : `Within income by ${formatEur(-data.difference)}`}
      </p>
    </section>
  )
}
