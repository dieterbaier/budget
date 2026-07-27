package eu.dieterbaier.budget.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import eu.dieterbaier.budget.adapter.out.persistence.CategoryGroupJpaRepository;
import eu.dieterbaier.budget.adapter.out.persistence.CategoryJpaRepository;
import eu.dieterbaier.budget.adapter.out.persistence.FixedCostJpaRepository;
import eu.dieterbaier.budget.adapter.out.persistence.IncomeEntryJpaRepository;
import eu.dieterbaier.budget.adapter.out.persistence.TransactionJpaRepository;
import eu.dieterbaier.budget.application.port.in.ManageCategoriesUseCase;
import eu.dieterbaier.budget.application.port.in.ManageCategoryGroupsUseCase;
import eu.dieterbaier.budget.application.port.in.NameInUseException;
import eu.dieterbaier.budget.application.port.in.RecordTransactionCommand;
import eu.dieterbaier.budget.application.port.in.RecordTransactionUseCase;
import eu.dieterbaier.budget.domain.model.Category;
import eu.dieterbaier.budget.domain.model.CategoryGroup;
import eu.dieterbaier.budget.domain.model.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Exercises the category use cases against a real PostgreSQL through the JPA
 * adapters.
 *
 * <p>The first test is the one that matters: ADR-021 claims a rename needs no
 * propagation logic because the persistence surrogate key carries every
 * reference. That is a claim about the adapter and the schema together, so an
 * in-memory test cannot check it — the Cucumber steps have to re-point their map
 * entries by hand, which is precisely the work this design avoids in production.
 */
@SpringBootTest
@Testcontainers
class CategoryPersistenceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ManageCategoryGroupsUseCase manageGroups;
    @Autowired
    private ManageCategoriesUseCase manageCategories;
    @Autowired
    private RecordTransactionUseCase recordTransaction;

    @Autowired
    private TransactionJpaRepository transactions;
    @Autowired
    private FixedCostJpaRepository fixedCosts;
    @Autowired
    private IncomeEntryJpaRepository incomeEntries;
    @Autowired
    private CategoryJpaRepository categories;
    @Autowired
    private CategoryGroupJpaRepository groups;

    @BeforeEach
    void clearDatabase() {
        transactions.deleteAll();
        fixedCosts.deleteAll();
        incomeEntries.deleteAll();
        categories.deleteAll();
        groups.deleteAll();
    }

    @Test
    void renamingACategoryKeepsTheTransactionsThatUseIt() {
        manageGroups.create("House");
        manageCategories.create("Grocries", "House", true);
        recordTransaction.record(new RecordTransactionCommand(
                LocalDate.of(2026, 7, 3), new BigDecimal("42.00"), "Grocries", TransactionType.EXPENSE));

        manageCategories.update("Grocries", "Groceries", "House", true);

        // The row is the same row: nothing in the domain moved the transaction.
        assertThat(transactions.countByCategoryName("Groceries")).isEqualTo(1);
        assertThat(transactions.countByCategoryName("Grocries")).isZero();
    }

    @Test
    void renamingAGroupKeepsItsCategories() {
        manageGroups.create("Huose");
        manageCategories.create("Groceries", "Huose", true);

        manageGroups.rename("Huose", "House");

        assertThat(manageCategories.list())
                .extracting(Category::group)
                .containsExactly(new CategoryGroup("House"));
    }

    @Test
    void aCategoryWithTransactionsCannotBeDeleted() {
        manageGroups.create("House");
        manageCategories.create("Groceries", "House", true);
        recordTransaction.record(new RecordTransactionCommand(
                LocalDate.of(2026, 7, 3), new BigDecimal("42.00"), "Groceries", TransactionType.EXPENSE));

        // The use case refuses before the database is asked to, so the message
        // names the count rather than a constraint (ADR-021).
        assertThatExceptionOfType(NameInUseException.class)
                .isThrownBy(() -> manageCategories.delete("Groceries"))
                .withMessageContaining("1 transaction");

        assertThat(categories.findByName("Groceries")).isPresent();
    }

    @Test
    void aGroupThatStillHoldsCategoriesCannotBeDeleted() {
        manageGroups.create("House");
        manageCategories.create("Groceries", "House", true);

        assertThatExceptionOfType(NameInUseException.class)
                .isThrownBy(() -> manageGroups.delete("House"))
                .withMessageContaining("1 category");
    }

    @Test
    void anUnusedCategoryAndThenItsGroupCanBeDeleted() {
        manageGroups.create("House");
        manageCategories.create("Mistake", "House", true);

        manageCategories.delete("Mistake");
        manageGroups.delete("House");

        assertThat(manageCategories.list()).isEmpty();
        assertThat(manageGroups.list()).isEmpty();
    }

    @Test
    void listsCategoriesWithTheirGroupInNameOrder() {
        manageGroups.create("House");
        manageGroups.create("Car");
        manageCategories.create("Groceries", "House", true);
        manageCategories.create("Fuel", "Car", false);

        assertThat(manageCategories.list())
                .extracting(Category::name)
                .containsExactly("Fuel", "Groceries");
        assertThat(manageGroups.list())
                .extracting(CategoryGroup::name)
                .containsExactly("Car", "House");
    }
}
