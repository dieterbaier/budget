package eu.dieterbaier.budget.application.service;

import eu.dieterbaier.budget.application.port.in.GetMonthlyExpenditureUseCase;
import eu.dieterbaier.budget.application.port.out.FixkostenpositionRepository;
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
    private final FixkostenpositionRepository fixkostenpositionen;
    private final IncomeRepository income;
    private final MonthlyExpenditureCalculator calculator;

    public MonthlyExpenditureService(
            TransactionRepository transactions,
            FixkostenpositionRepository fixkostenpositionen,
            IncomeRepository income) {
        this.transactions = transactions;
        this.fixkostenpositionen = fixkostenpositionen;
        this.income = income;
        this.calculator = new MonthlyExpenditureCalculator();
    }

    @Override
    public MonthlyExpenditure forMonth(YearMonth month) {
        return calculator.calculate(
                month,
                transactions.findByMonth(month),
                fixkostenpositionen.findAll(),
                income.averageMonthlyIncome());
    }
}
