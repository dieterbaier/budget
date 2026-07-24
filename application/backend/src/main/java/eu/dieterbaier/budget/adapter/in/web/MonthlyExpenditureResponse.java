package eu.dieterbaier.budget.adapter.in.web;

import eu.dieterbaier.budget.domain.service.MonthlyExpenditure;

import java.math.BigDecimal;

/**
 * API representation of the current monthly expenditure. This boundary DTO keeps
 * the domain records out of the JSON contract; amounts are exact decimals in EUR.
 */
public record MonthlyExpenditureResponse(
        String month,
        BigDecimal variableCosts,
        BigDecimal fixedCostsMonthly,
        BigDecimal total,
        BigDecimal averageIncome,
        BigDecimal difference,
        boolean overspending) {

    public static MonthlyExpenditureResponse from(MonthlyExpenditure expenditure) {
        return new MonthlyExpenditureResponse(
                expenditure.month().toString(),
                expenditure.variableCosts().amount(),
                expenditure.fixedCostsMonthly().amount(),
                expenditure.total().amount(),
                expenditure.averageIncome().amount(),
                expenditure.difference().amount(),
                expenditure.isOverspending());
    }
}
