package eu.dieterbaier.budget.domain.model;

/**
 * Kind of a recorded transaction. Only EXPENSE amounts count towards monthly
 * expenditure; INCOME feeds the income figures and TRANSFER (money moved between
 * the owner's accounts) is never an expense. A refund is an EXPENSE with a
 * negative amount in the same category.
 */
public enum TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}
