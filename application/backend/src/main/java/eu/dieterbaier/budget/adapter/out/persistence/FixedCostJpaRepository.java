package eu.dieterbaier.budget.adapter.out.persistence;

import eu.dieterbaier.budget.adapter.out.persistence.entity.FixedCostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FixedCostJpaRepository extends JpaRepository<FixedCostEntity, Long> {
}
