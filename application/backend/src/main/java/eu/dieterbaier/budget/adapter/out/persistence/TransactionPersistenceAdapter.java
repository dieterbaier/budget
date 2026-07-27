package eu.dieterbaier.budget.adapter.out.persistence;

import eu.dieterbaier.budget.adapter.out.persistence.entity.CategoryEntity;
import eu.dieterbaier.budget.adapter.out.persistence.entity.TransactionEntity;
import eu.dieterbaier.budget.application.port.out.TransactionRepository;
import eu.dieterbaier.budget.domain.model.Category;
import eu.dieterbaier.budget.domain.model.CategoryGroup;
import eu.dieterbaier.budget.domain.model.Money;
import eu.dieterbaier.budget.domain.model.Transaction;
import eu.dieterbaier.budget.domain.model.TransactionType;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;

/** Outbound adapter implementing the transaction repository port on top of JPA. */
@Repository
public class TransactionPersistenceAdapter implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;
    private final CategoryJpaRepository categoryJpaRepository;

    public TransactionPersistenceAdapter(TransactionJpaRepository jpaRepository,
                                         CategoryJpaRepository categoryJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.categoryJpaRepository = categoryJpaRepository;
    }

    @Override
    public List<Transaction> findByMonth(YearMonth month) {
        return jpaRepository.findByBookingDateBetween(month.atDay(1), month.atEndOfMonth())
                .stream()
                .map(TransactionPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public void save(Transaction transaction) {
        CategoryEntity category = categoryJpaRepository.findByName(transaction.category().name())
                .orElseThrow(() -> new IllegalStateException(
                        "Category not found while saving transaction: " + transaction.category().name()));

        jpaRepository.save(new TransactionEntity(
                transaction.date(),
                transaction.amount().amount(),
                category,
                transaction.type().name()));
    }

    private static Transaction toDomain(TransactionEntity entity) {
        return new Transaction(
                entity.getBookingDate(),
                new Money(entity.getAmount()),
                toDomain(entity.getCategory()),
                TransactionType.valueOf(entity.getTransactionType()));
    }

    static Category toDomain(CategoryEntity category) {
        return new Category(
                category.getName(),
                new CategoryGroup(category.getGroup().getName()),
                category.isPensionRelevant());
    }
}
