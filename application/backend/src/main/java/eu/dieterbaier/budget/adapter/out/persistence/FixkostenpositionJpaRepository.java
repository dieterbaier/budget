package eu.dieterbaier.budget.adapter.out.persistence;

import eu.dieterbaier.budget.adapter.out.persistence.entity.FixkostenpositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FixkostenpositionJpaRepository extends JpaRepository<FixkostenpositionEntity, Long> {

    long countByCategoryName(String categoryName);
}
