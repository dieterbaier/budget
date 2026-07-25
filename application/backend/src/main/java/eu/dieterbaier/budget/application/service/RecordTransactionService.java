package eu.dieterbaier.budget.application.service;

import eu.dieterbaier.budget.application.port.in.RecordTransactionCommand;
import eu.dieterbaier.budget.application.port.in.RecordTransactionUseCase;
import eu.dieterbaier.budget.application.port.in.UnknownCategoryException;
import eu.dieterbaier.budget.application.port.out.CategoryRepository;
import eu.dieterbaier.budget.application.port.out.TransactionRepository;
import eu.dieterbaier.budget.domain.model.Category;
import eu.dieterbaier.budget.domain.model.Money;
import eu.dieterbaier.budget.domain.model.Transaction;

/**
 * Application service that records a transaction. It enforces the precondition
 * that the referenced category exists (master data) before persisting through
 * the transaction repository port. Framework-free; wired in UseCaseConfig.
 */
public class RecordTransactionService implements RecordTransactionUseCase {

    private final CategoryRepository categories;
    private final TransactionRepository transactions;

    public RecordTransactionService(CategoryRepository categories, TransactionRepository transactions) {
        this.categories = categories;
        this.transactions = transactions;
    }

    @Override
    public Transaction record(RecordTransactionCommand command) {
        Category category = categories.findByName(command.categoryName())
                .orElseThrow(() -> new UnknownCategoryException(command.categoryName()));

        Transaction transaction = new Transaction(
                command.date(),
                new Money(command.amount()),
                category,
                command.type());

        transactions.save(transaction);
        return transaction;
    }
}
