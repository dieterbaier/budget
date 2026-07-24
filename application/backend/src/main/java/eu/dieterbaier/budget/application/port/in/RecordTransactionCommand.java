package eu.dieterbaier.budget.application.port.in;

import eu.dieterbaier.budget.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Input for recording a transaction from a bank statement. The category is
 * referenced by name (it must already exist as master data). A refund is an
 * EXPENSE with a negative amount.
 */
public record RecordTransactionCommand(LocalDate date, BigDecimal amount, String categoryName, TransactionType type) {
}
