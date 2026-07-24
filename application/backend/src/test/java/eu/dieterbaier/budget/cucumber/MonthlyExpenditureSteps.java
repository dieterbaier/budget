package eu.dieterbaier.budget.cucumber;

import eu.dieterbaier.budget.application.port.in.GetMonthlyExpenditureUseCase;
import eu.dieterbaier.budget.application.port.out.FixedCostRepository;
import eu.dieterbaier.budget.application.port.out.IncomeRepository;
import eu.dieterbaier.budget.application.port.out.TransactionRepository;
import eu.dieterbaier.budget.application.service.MonthlyExpenditureService;
import eu.dieterbaier.budget.domain.model.Category;
import eu.dieterbaier.budget.domain.model.FixedCost;
import eu.dieterbaier.budget.domain.model.Money;
import eu.dieterbaier.budget.domain.model.PaymentInterval;
import eu.dieterbaier.budget.domain.model.Transaction;
import eu.dieterbaier.budget.domain.model.TransactionType;
import eu.dieterbaier.budget.domain.service.MonthlyExpenditure;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Drives the monthly-expenditure use case through its inbound port, backed by
 * in-memory implementations of the outbound ports — the same hexagonal seams a
 * real persistence adapter will plug into.
 */
public class MonthlyExpenditureSteps {

    private final List<Transaction> transactions = new ArrayList<>();
    private final List<FixedCost> fixedCosts = new ArrayList<>();
    private Money averageIncome = Money.ZERO;
    private MonthlyExpenditure result;

    @ParameterType("\\d+(?:\\.\\d{1,2})?")
    public Money money(String amount) {
        return Money.of(amount);
    }

    @ParameterType("\\d{4}-\\d{2}")
    public YearMonth yearMonth(String value) {
        return YearMonth.parse(value);
    }

    @ParameterType("monthly|quarterly|half-yearly|yearly")
    public PaymentInterval interval(String value) {
        return switch (value) {
            case "monthly" -> PaymentInterval.MONTHLY;
            case "quarterly" -> PaymentInterval.QUARTERLY;
            case "half-yearly" -> PaymentInterval.HALF_YEARLY;
            case "yearly" -> PaymentInterval.YEARLY;
            default -> throw new IllegalArgumentException("Unknown interval: " + value);
        };
    }

    @Given("the average monthly income is {money} EUR")
    public void theAverageMonthlyIncomeIs(Money income) {
        this.averageIncome = income;
    }

    @Given("a {interval} fixed cost {string} of {money} EUR")
    public void aFixedCostOf(PaymentInterval interval, String name, Money amount) {
        fixedCosts.add(new FixedCost(name, amount, interval, Category.of(name), null));
    }

    @Given("in month {yearMonth} the following expenses:")
    public void inMonthTheFollowingExpenses(YearMonth month, DataTable table) {
        for (Map<String, String> row : table.asMaps()) {
            transactions.add(new Transaction(
                    month.atDay(1),
                    Money.of(row.get("amount")),
                    Category.of(row.get("category")),
                    TransactionType.EXPENSE));
        }
    }

    @Given("in month {yearMonth} a refund of {money} EUR in category {string}")
    public void inMonthARefundOf(YearMonth month, Money amount, String category) {
        transactions.add(new Transaction(
                month.atDay(1),
                amount.negate(),
                Category.of(category),
                TransactionType.EXPENSE));
    }

    @Given("in month {yearMonth} a transfer of {money} EUR")
    public void inMonthATransferOf(YearMonth month, Money amount) {
        transactions.add(new Transaction(
                month.atDay(1),
                amount,
                Category.of("Transfer"),
                TransactionType.TRANSFER));
    }

    @When("I calculate the monthly expenditure for {yearMonth}")
    public void iCalculateTheMonthlyExpenditureFor(YearMonth month) {
        TransactionRepository transactionRepository = queryMonth ->
                transactions.stream().filter(t -> YearMonth.from(t.date()).equals(queryMonth)).toList();
        FixedCostRepository fixedCostRepository = () -> List.copyOf(fixedCosts);
        IncomeRepository incomeRepository = () -> averageIncome;

        GetMonthlyExpenditureUseCase useCase =
                new MonthlyExpenditureService(transactionRepository, fixedCostRepository, incomeRepository);
        result = useCase.forMonth(month);
    }

    @Then("the fixed costs share is {money} EUR")
    public void theFixedCostsShareIs(Money expected) {
        assertThat(result.fixedCostsMonthly()).isEqualTo(expected);
    }

    @Then("the variable costs are {money} EUR")
    public void theVariableCostsAre(Money expected) {
        assertThat(result.variableCosts()).isEqualTo(expected);
    }

    @Then("the total expenditure is {money} EUR")
    public void theTotalExpenditureIs(Money expected) {
        assertThat(result.total()).isEqualTo(expected);
    }

    @Then("it is overspending")
    public void itIsOverspending() {
        assertThat(result.isOverspending()).isTrue();
    }

    @Then("it is not overspending")
    public void itIsNotOverspending() {
        assertThat(result.isOverspending()).isFalse();
    }
}
