package eu.dieterbaier.budget.adapter.out.persistence;

import eu.dieterbaier.budget.adapter.out.persistence.entity.FixkostenpositionEntity;
import eu.dieterbaier.budget.application.port.out.FixkostenpositionRepository;
import eu.dieterbaier.budget.domain.model.Fixkostenposition;
import eu.dieterbaier.budget.domain.model.Money;
import eu.dieterbaier.budget.domain.model.Zahlungsintervall;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Outbound adapter implementing the Fixkostenposition repository port on top of JPA. */
@Repository
public class FixkostenpositionPersistenceAdapter implements FixkostenpositionRepository {

    private final FixkostenpositionJpaRepository jpaRepository;

    public FixkostenpositionPersistenceAdapter(FixkostenpositionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Fixkostenposition> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(FixkostenpositionPersistenceAdapter::toDomain)
                .toList();
    }

    private static Fixkostenposition toDomain(FixkostenpositionEntity entity) {
        return new Fixkostenposition(
                entity.getName(),
                new Money(entity.getAmount()),
                Zahlungsintervall.valueOf(entity.getZahlungsintervall()),
                TransactionPersistenceAdapter.toDomain(entity.getCategory()),
                entity.getLetzteZahlung());
    }
}
