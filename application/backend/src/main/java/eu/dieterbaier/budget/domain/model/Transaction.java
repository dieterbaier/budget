package eu.dieterbaier.budget.domain.model;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * A single recorded transaction taken from a bank statement. Refunds are
 * expenses with a negative amount in the same category.
 */
public record Transaction(LocalDate date, Money amount, Category category, TransactionType type) {

    public boolean isExpenseIn(YearMonth month) {
        return type == TransactionType.EXPENSE && YearMonth.from(date).equals(month);
    }
}
