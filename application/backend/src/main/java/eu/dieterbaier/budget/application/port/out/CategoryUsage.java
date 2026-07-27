package eu.dieterbaier.budget.application.port.out;

/**
 * Outbound port answering whether master data is still referenced. It exists
 * because "a category in use cannot be deleted" spans aggregates, so it cannot
 * be an invariant of the category itself (ADR-021): the use case asks this port
 * and rejects the deletion, rather than a foreign key rejecting it later in a
 * language the owner does not read.
 */
public interface CategoryUsage {

    /** How many transactions reference the category with this name. */
    long countTransactionsIn(String categoryName);

    /** How many fixed-cost definitions reference the category with this name. */
    long countFixedCostsIn(String categoryName);

    /** How many categories belong to the group with this name. */
    long countCategoriesInGroup(String groupName);
}
