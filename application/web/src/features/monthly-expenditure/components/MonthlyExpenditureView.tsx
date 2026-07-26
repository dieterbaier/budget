import { formatEur } from '@/shared/format/money'
import { useMonthlyExpenditure } from '../queries/useMonthlyExpenditure'

interface Props {
  month: string
}

export function MonthlyExpenditureView({ month }: Props) {
  const { data, error, isPending } = useMonthlyExpenditure(month)

  if (isPending) return <p>Loading…</p>
  if (error) return <p role="alert">{error.message}</p>
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
