package eu.dieterbaier.budget.adapter.in.web;

import eu.dieterbaier.budget.application.port.in.RecordTransactionCommand;
import eu.dieterbaier.budget.domain.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request body for recording a transaction. Amount may be negative (a refund).
 */
public record RecordTransactionRequest(
        @NotNull LocalDate date,
        @NotNull BigDecimal amount,
        @NotBlank String category,
        @NotNull TransactionType type) {

    public RecordTransactionCommand toCommand() {
        return new RecordTransactionCommand(date, amount, category, type);
    }
}
