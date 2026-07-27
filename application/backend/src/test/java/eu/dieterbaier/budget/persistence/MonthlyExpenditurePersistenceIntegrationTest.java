package eu.dieterbaier.budget.persistence;

import eu.dieterbaier.budget.adapter.out.persistence.CategoryJpaRepository;
import eu.dieterbaier.budget.adapter.out.persistence.CategoryGroupJpaRepository;
import eu.dieterbaier.budget.adapter.out.persistence.FixedCostJpaRepository;
import eu.dieterbaier.budget.adapter.out.persistence.IncomeEntryJpaRepository;
import eu.dieterbaier.budget.adapter.out.persistence.TransactionJpaRepository;
import eu.dieterbaier.budget.adapter.out.persistence.entity.CategoryEntity;
import eu.dieterbaier.budget.adapter.out.persistence.entity.CategoryGroupEntity;
import eu.dieterbaier.budget.adapter.out.persistence.entity.FixedCostEntity;
import eu.dieterbaier.budget.adapter.out.persistence.entity.IncomeEntryEntity;
import eu.dieterbaier.budget.adapter.out.persistence.entity.TransactionEntity;
import eu.dieterbaier.budget.application.port.in.GetMonthlyExpenditureUseCase;
import eu.dieterbaier.budget.application.port.in.RecordTransactionCommand;
import eu.dieterbaier.budget.application.port.in.RecordTransactionUseCase;
import eu.dieterbaier.budget.domain.model.Money;
import eu.dieterbaier.budget.domain.model.TransactionType;
import eu.dieterbaier.budget.domain.service.MonthlyExpenditure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the full outbound chain against a real PostgreSQL: Flyway migrates the
 * schema, the JPA entities and adapters map to the domain, and the use case
 * computes the monthly expenditure from persisted data.
 */
@SpringBootTest
@Testcontainers
class MonthlyExpenditurePersistenceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private GetMonthlyExpenditureUseCase useCase;
    @Autowired
    private RecordTransactionUseCase recordTransaction;
    @Autowired
    private CategoryGroupJpaRepository groups;
    @Autowired
    private CategoryJpaRepository categories;
    @Autowired
    private TransactionJpaRepository transactions;
    @Autowired
    private FixedCostJpaRepository fixedCosts;
    @Autowired
    private IncomeEntryJpaRepository incomeEntries;

    @BeforeEach
    void clearDatabase() {
        transactions.deleteAll();
        fixedCosts.deleteAll();
        incomeEntries.deleteAll();
        categories.deleteAll();
        groups.deleteAll();
    }

    @Test
    void computesMonthlyExpenditureFromPostgres() {
        CategoryGroupEntity household = groups.save(new CategoryGroupEntity("Household"));
        CategoryEntity groceries = categories.save(new CategoryEntity("Groceries", household, true));
        CategoryEntity car = categories.save(new CategoryEntity("Car", household, false));

        transactions.save(expense(LocalDate.of(2026, 7, 3), "800.00", groceries));
        transactions.save(expense(LocalDate.of(2026, 7, 20), "150.00", groceries));
        transactions.save(expense(LocalDate.of(2026, 7, 10), "-50.00", groceries)); // refund
        transactions.save(new TransactionEntity(LocalDate.of(2026, 7, 5), new BigDecimal("500.00"), groceries, "TRANSFER"));
        transactions.save(expense(LocalDate.of(2026, 6, 30), "999.00", groceries)); // other month

        fixedCosts.save(new FixedCostEntity("Car insurance", new BigDecimal("1200.00"), "YEARLY", car, LocalDate.of(2026, 3, 15)));

        incomeEntries.save(new IncomeEntryEntity(LocalDate.of(2026, 7, 1), new BigDecimal("900.00")));
        incomeEntries.save(new IncomeEntryEntity(LocalDate.of(2026, 6, 1), new BigDecimal("1000.00"))); // average 950

        MonthlyExpenditure result = useCase.forMonth(YearMonth.of(2026, 7));

        assertThat(result.variableCosts()).isEqualTo(Money.of("900.00"));
        assertThat(result.fixedCostsMonthly()).isEqualTo(Money.of("100.00"));
        assertThat(result.total()).isEqualTo(Money.of("1000.00"));
        assertThat(result.averageIncome()).isEqualTo(Money.of("950.00"));
        assertThat(result.isOverspending()).isTrue();
    }

    @Test
    void recordsTransactionThroughWritePortAndReflectsInExpenditure() {
        CategoryGroupEntity household = groups.save(new CategoryGroupEntity("Household"));
        categories.save(new CategoryEntity("Groceries", household, true));

        recordTransaction.record(new RecordTransactionCommand(
                LocalDate.of(2026, 7, 3), new BigDecimal("250.00"), "Groceries", TransactionType.EXPENSE));

        MonthlyExpenditure result = useCase.forMonth(YearMonth.of(2026, 7));

        assertThat(result.variableCosts()).isEqualTo(Money.of("250.00"));
        assertThat(result.total()).isEqualTo(Money.of("250.00"));
    }

    private static TransactionEntity expense(LocalDate date, String amount, CategoryEntity category) {
        return new TransactionEntity(date, new BigDecimal(amount), category, "EXPENSE");
    }
}
