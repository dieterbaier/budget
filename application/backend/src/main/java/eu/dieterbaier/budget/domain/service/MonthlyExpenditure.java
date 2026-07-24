package eu.dieterbaier.budget.domain.service;

import eu.dieterbaier.budget.domain.model.Money;

import java.time.YearMonth;

/**
 * Result of the current monthly expenditure calculation: the variable costs of
 * the month, the amortized monthly share of all fixed costs, their total, and
 * the comparison against average monthly income (the overspending signal).
 */
public record MonthlyExpenditure(
        YearMonth month,
        Money variableCosts,
        Money fixedCostsMonthly,
        Money total,
        Money averageIncome) {

    public Money difference() {
        return total.subtract(averageIncome);
    }

    public boolean isOverspending() {
        return total.isGreaterThan(averageIncome);
    }
}
