import { formatEur } from '@/shared/format/money'
import { useMonthlyExpenditure } from '../queries/useMonthlyExpenditure'

interface Props {
  month: string
}

export function MonthlyExpenditureView({ month }: Props) {
  const { data, error, isPending } = useMonthlyExpenditure(month)

  if (isPending) return <p>Loading…</p>
  if (error) return <p role="alert" className="text-danger">{error.message}</p>
  if (!data) return null

  return (
    <section aria-label="Current monthly expenditure">
      <dl className="grid grid-cols-2 gap-3 m-0">
        <Figure label="Variable costs" amount={data.variableCosts} />
        <Figure label="Fixed costs (monthly)" amount={data.fixedCostsMonthly} />
        <Figure label="Total" amount={data.total} />
        <Figure label="Average income" amount={data.averageIncome} />
      </dl>
      <p
        role="status"
        className={`mt-4 p-3 rounded-lg font-semibold ${
          data.overspending ? 'bg-danger-soft text-danger' : 'bg-good-soft text-good'
        }`}
      >
        {data.overspending
          ? `Overspending by ${formatEur(data.difference)}`
          : `Within income by ${formatEur(-data.difference)}`}
      </p>
    </section>
  )
}

function Figure({ label, amount }: { label: string; amount: number }) {
  return (
    <div className="bg-white border border-hairline rounded-lg p-3">
      <dt className="text-xs text-muted">{label}</dt>
      <dd className="mt-1 mx-0 mb-0 text-xl font-semibold">{formatEur(amount)}</dd>
    </div>
  )
}
