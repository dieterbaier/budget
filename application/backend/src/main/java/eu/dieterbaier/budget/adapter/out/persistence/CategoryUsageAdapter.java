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
    private final FixedCostJpaRepository fixedCosts;
    private final CategoryJpaRepository categories;

    public CategoryUsageAdapter(
            TransactionJpaRepository transactions,
            FixedCostJpaRepository fixedCosts,
            CategoryJpaRepository categories) {
        this.transactions = transactions;
        this.fixedCosts = fixedCosts;
        this.categories = categories;
    }

    @Override
    public long countTransactionsIn(String categoryName) {
        return transactions.countByCategoryName(categoryName);
    }

    @Override
    public long countFixedCostsIn(String categoryName) {
        return fixedCosts.countByCategoryName(categoryName);
    }

    @Override
    public long countCategoriesInGroup(String groupName) {
        return categories.countByGroupName(groupName);
    }
}
