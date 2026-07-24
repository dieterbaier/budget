package eu.dieterbaier.budget.domain;

import eu.dieterbaier.budget.domain.model.Category;
import eu.dieterbaier.budget.domain.model.FixedCost;
import eu.dieterbaier.budget.domain.model.Money;
import eu.dieterbaier.budget.domain.model.PaymentInterval;
import eu.dieterbaier.budget.domain.model.Transaction;
import eu.dieterbaier.budget.domain.model.TransactionType;
import eu.dieterbaier.budget.domain.service.MonthlyExpenditure;
import eu.dieterbaier.budget.domain.service.MonthlyExpenditureCalculator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlyExpenditureCalculatorTest {

    private static final YearMonth JULY = YearMonth.of(2026, 7);
    private static final Category GROCERIES = Category.of("Groceries");
    private static final Category FUEL = Category.of("Fuel");

    private final MonthlyExpenditureCalculator calculator = new MonthlyExpenditureCalculator();

    private Transaction expense(String amount, Category category, LocalDate date) {
        return new Transaction(date, Money.of(amount), category, TransactionType.EXPENSE);
    }

    @Test
    void sumsVariableExpensesOfTheMonth() {
        List<Transaction> transactions = List.of(
                expense("800", GROCERIES, LocalDate.of(2026, 7, 3)),
                expense("150", FUEL, LocalDate.of(2026, 7, 20)));

        MonthlyExpenditure result = calculator.calculate(JULY, transactions, List.of(), Money.of("2000"));

        assertThat(result.variableCosts()).isEqualTo(Money.of("950.00"));
    }

    @Test
    void treatsRefundAsNegativeExpenseInTheCategory() {
        List<Transaction> transactions = List.of(
                expense("800", GROCERIES, LocalDate.of(2026, 7, 3)),
                expense("-50", GROCERIES, LocalDate.of(2026, 7, 10)));

        MonthlyExpenditure result = calculator.calculate(JULY, transactions, List.of(), Money.of("2000"));

        assertThat(result.variableCosts()).isEqualTo(Money.of("750.00"));
    }

    @Test
    void ignoresTransfersAndIncome() {
        List<Transaction> transactions = List.of(
                expense("200", GROCERIES, LocalDate.of(2026, 7, 3)),
                new Transaction(LocalDate.of(2026, 7, 4), Money.of("500"), GROCERIES, TransactionType.TRANSFER),
                new Transaction(LocalDate.of(2026, 7, 5), Money.of("2000"), GROCERIES, TransactionType.INCOME));

        MonthlyExpenditure result = calculator.calculate(JULY, transactions, List.of(), Money.of("2000"));

        assertThat(result.variableCosts()).isEqualTo(Money.of("200.00"));
    }

    @Test
    void ignoresExpensesFromOtherMonths() {
        List<Transaction> transactions = List.of(
                expense("200", GROCERIES, LocalDate.of(2026, 7, 3)),
                expense("999", GROCERIES, LocalDate.of(2026, 6, 30)));

        MonthlyExpenditure result = calculator.calculate(JULY, transactions, List.of(), Money.of("2000"));

        assertThat(result.variableCosts()).isEqualTo(Money.of("200.00"));
    }

    @Test
    void addsAmortizedFixedCostsToTheTotal() {
        FixedCost carInsurance = new FixedCost(
                "Car insurance", Money.of("1200"), PaymentInterval.YEARLY, FUEL, LocalDate.of(2026, 3, 15));

        MonthlyExpenditure result = calculator.calculate(
                JULY,
                List.of(expense("900", GROCERIES, LocalDate.of(2026, 7, 3))),
                List.of(carInsurance),
                Money.of("2000"));

        assertThat(result.fixedCostsMonthly()).isEqualTo(Money.of("100.00"));
        assertThat(result.total()).isEqualTo(Money.of("1000.00"));
    }

    @Test
    void flagsOverspendingWhenTotalExceedsAverageIncome() {
        MonthlyExpenditure result = calculator.calculate(
                JULY,
                List.of(expense("1000", GROCERIES, LocalDate.of(2026, 7, 3))),
                List.of(),
                Money.of("950"));

        assertThat(result.isOverspending()).isTrue();
        assertThat(result.difference()).isEqualTo(Money.of("50.00"));
    }

    @Test
    void doesNotFlagOverspendingWhenWithinIncome() {
        MonthlyExpenditure result = calculator.calculate(
                JULY,
                List.of(expense("900", GROCERIES, LocalDate.of(2026, 7, 3))),
                List.of(),
                Money.of("2000"));

        assertThat(result.isOverspending()).isFalse();
    }
}
