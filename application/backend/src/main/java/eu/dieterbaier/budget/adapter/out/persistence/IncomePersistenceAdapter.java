package eu.dieterbaier.budget.adapter.out.persistence;

import eu.dieterbaier.budget.application.port.out.IncomeRepository;
import eu.dieterbaier.budget.domain.model.Money;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/** Outbound adapter implementing the income repository port on top of JPA. */
@Repository
public class IncomePersistenceAdapter implements IncomeRepository {

    private final IncomeEntryJpaRepository jpaRepository;

    public IncomePersistenceAdapter(IncomeEntryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Money averageMonthlyIncome() {
        BigDecimal average = jpaRepository.averageAmount();
        return average == null ? Money.ZERO : new Money(average);
    }
}
