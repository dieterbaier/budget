package eu.dieterbaier.budget.domain.service;

import eu.dieterbaier.budget.domain.model.FixedCost;
import eu.dieterbaier.budget.domain.model.Money;
import eu.dieterbaier.budget.domain.model.Transaction;

import java.time.YearMonth;
import java.util.List;

/**
 * Pure domain rule for the "current monthly expenditure" view (ADR-003).
 *
 * <p>Variable costs are the month's EXPENSE transactions (refunds are negative
 * expenses that net against their category; transfers and income are excluded).
 * Fixed costs are amortized to their monthly portion. The total is compared to
 * the average monthly income to produce the overspending signal.
 */
public class MonthlyExpenditureCalculator {

    public MonthlyExpenditure calculate(
            YearMonth month,
            List<Transaction> transactions,
            List<FixedCost> fixedCosts,
            Money averageIncome) {

        Money variableCosts = transactions.stream()
                .filter(transaction -> transaction.isExpenseIn(month))
                .map(Transaction::amount)
                .reduce(Money.ZERO, Money::add);

        Money fixedCostsMonthly = fixedCosts.stream()
                .map(FixedCost::monthlyPortion)
                .reduce(Money.ZERO, Money::add);

        Money total = variableCosts.add(fixedCostsMonthly);

        return new MonthlyExpenditure(month, variableCosts, fixedCostsMonthly, total, averageIncome);
    }
}
