package eu.dieterbaier.budget.adapter.in.web;

import eu.dieterbaier.budget.domain.model.Transaction;

import java.math.BigDecimal;

/** API representation of a recorded transaction. */
public record TransactionResponse(String date, BigDecimal amount, String category, String type) {

    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.date().toString(),
                transaction.amount().amount(),
                transaction.category().name(),
                transaction.type().name());
    }
}
