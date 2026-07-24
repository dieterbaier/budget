package eu.dieterbaier.budget.adapter.out.persistence;

import eu.dieterbaier.budget.adapter.out.persistence.entity.FixedCostEntity;
import eu.dieterbaier.budget.application.port.out.FixedCostRepository;
import eu.dieterbaier.budget.domain.model.FixedCost;
import eu.dieterbaier.budget.domain.model.Money;
import eu.dieterbaier.budget.domain.model.PaymentInterval;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Outbound adapter implementing the fixed-cost repository port on top of JPA. */
@Repository
public class FixedCostPersistenceAdapter implements FixedCostRepository {

    private final FixedCostJpaRepository jpaRepository;

    public FixedCostPersistenceAdapter(FixedCostJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<FixedCost> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(FixedCostPersistenceAdapter::toDomain)
                .toList();
    }

    private static FixedCost toDomain(FixedCostEntity entity) {
        return new FixedCost(
                entity.getName(),
                new Money(entity.getAmount()),
                PaymentInterval.valueOf(entity.getPaymentInterval()),
                TransactionPersistenceAdapter.toDomain(entity.getCategory()),
                entity.getAnchorDate());
    }
}
