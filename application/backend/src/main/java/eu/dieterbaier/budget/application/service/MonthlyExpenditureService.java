package eu.dieterbaier.budget.application.service;

import eu.dieterbaier.budget.application.port.in.GetMonthlyExpenditureUseCase;
import eu.dieterbaier.budget.application.port.out.FixedCostRepository;
import eu.dieterbaier.budget.application.port.out.IncomeRepository;
import eu.dieterbaier.budget.application.port.out.TransactionRepository;
import eu.dieterbaier.budget.domain.service.MonthlyExpenditure;
import eu.dieterbaier.budget.domain.service.MonthlyExpenditureCalculator;

import java.time.YearMonth;

/**
 * Application service implementing the monthly-expenditure query. It loads data
 * through outbound ports and delegates the rule to the domain calculator; it
 * contains orchestration only, no money rules and no infrastructure.
 */
public class MonthlyExpenditureService implements GetMonthlyExpenditureUseCase {

    private final TransactionRepository transactions;
    private final FixedCostRepository fixedCosts;
    private final IncomeRepository income;
    private final MonthlyExpenditureCalculator calculator;

    public MonthlyExpenditureService(
            TransactionRepository transactions,
            FixedCostRepository fixedCosts,
            IncomeRepository income) {
        this.transactions = transactions;
        this.fixedCosts = fixedCosts;
        this.income = income;
        this.calculator = new MonthlyExpenditureCalculator();
    }

    @Override
    public MonthlyExpenditure forMonth(YearMonth month) {
        return calculator.calculate(
                month,
                transactions.findByMonth(month),
                fixedCosts.findAll(),
                income.averageMonthlyIncome());
    }
}
