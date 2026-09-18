package eu.dieterbaier.budget.adapter.out.persistence;

import eu.dieterbaier.budget.application.port.out.CategoryUsage;
import org.springframework.stereotype.Repository;

/**
 * Answers what still references a category or a group, so the use case can
 * refuse a deletion in the owner's terms instead of letting a foreign key refuse
 * it in the driver's (ADR-021).
 */
@Repository
public class CategoryUsageAdapter implements CategoryUsage {

    private final TransactionJpaRepository transactions;
    private final FixkostenpositionJpaRepository fixkostenpositionen;
    private final CategoryJpaRepository categories;

    public CategoryUsageAdapter(
            TransactionJpaRepository transactions,
            FixkostenpositionJpaRepository fixkostenpositionen,
            CategoryJpaRepository categories) {
        this.transactions = transactions;
        this.fixkostenpositionen = fixkostenpositionen;
        this.categories = categories;
    }

    @Override
    public long countTransactionsIn(String categoryName) {
        return transactions.countByCategoryName(categoryName);
    }

    @Override
    public long countFixkostenpositionenIn(String categoryName) {
        return fixkostenpositionen.countByCategoryName(categoryName);
    }

    @Override
    public long countCategoriesInGroup(String groupName) {
        return categories.countByGroupName(groupName);
    }
}
