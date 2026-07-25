package eu.dieterbaier.budget.dev;

import eu.dieterbaier.budget.adapter.out.persistence.CategoryJpaRepository;
import eu.dieterbaier.budget.adapter.out.persistence.FixedCostJpaRepository;
import eu.dieterbaier.budget.adapter.out.persistence.IncomeEntryJpaRepository;
import eu.dieterbaier.budget.adapter.out.persistence.TransactionJpaRepository;
import eu.dieterbaier.budget.adapter.out.persistence.entity.CategoryEntity;
import eu.dieterbaier.budget.adapter.out.persistence.entity.FixedCostEntity;
import eu.dieterbaier.budget.adapter.out.persistence.entity.IncomeEntryEntity;
import eu.dieterbaier.budget.adapter.out.persistence.entity.TransactionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seeds sample data for local exploration only ({@code local} profile). It runs
 * on {@code ./gradlew bootRun} so the monthly-expenditure endpoint returns real
 * numbers. The figures mirror the tests: month 2026-07 has 900 variable + 100
 * amortized fixed = 1000 total against an average income of 950 (overspending).
 *
 * <p>This class writes JPA entities directly instead of going through the
 * outbound ports, which CON-003 otherwise forbids. It is the one exception that
 * constraint allows, on the grounds that it is local-profile-only and never
 * appears in a request path — see CON-003 and ADR-013. Anything that ships to a
 * user goes through a port.
 */
@Component
@Profile("local")
public class LocalDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LocalDataSeeder.class);

    private final CategoryJpaRepository categories;
    private final TransactionJpaRepository transactions;
    private final FixedCostJpaRepository fixedCosts;
    private final IncomeEntryJpaRepository incomeEntries;

    public LocalDataSeeder(CategoryJpaRepository categories, TransactionJpaRepository transactions,
                           FixedCostJpaRepository fixedCosts, IncomeEntryJpaRepository incomeEntries) {
        this.categories = categories;
        this.transactions = transactions;
        this.fixedCosts = fixedCosts;
        this.incomeEntries = incomeEntries;
    }

    @Override
    public void run(String... args) {
        if (categories.count() > 0) {
            log.info("Local data already present; skipping seed.");
            return;
        }

        CategoryEntity groceries = categories.save(new CategoryEntity("Groceries", true));
        CategoryEntity car = categories.save(new CategoryEntity("Car", false));

        transactions.save(expense(LocalDate.of(2026, 7, 3), "800.00", groceries));
        transactions.save(expense(LocalDate.of(2026, 7, 20), "150.00", groceries));
        transactions.save(expense(LocalDate.of(2026, 7, 10), "-50.00", groceries)); // refund
        transactions.save(new TransactionEntity(LocalDate.of(2026, 7, 5), new BigDecimal("500.00"), groceries, "TRANSFER"));
        transactions.save(expense(LocalDate.of(2026, 6, 30), "999.00", groceries)); // previous month

        fixedCosts.save(new FixedCostEntity("Car insurance", new BigDecimal("1200.00"), "YEARLY", car, LocalDate.of(2026, 3, 15)));

        incomeEntries.save(new IncomeEntryEntity(LocalDate.of(2026, 7, 1), new BigDecimal("900.00")));
        incomeEntries.save(new IncomeEntryEntity(LocalDate.of(2026, 6, 1), new BigDecimal("1000.00")));

        log.info("Seeded local demo data. Try: GET /api/monthly-expenditure?month=2026-07");
    }

    private static TransactionEntity expense(LocalDate date, String amount, CategoryEntity category) {
        return new TransactionEntity(date, new BigDecimal(amount), category, "EXPENSE");
    }
}
