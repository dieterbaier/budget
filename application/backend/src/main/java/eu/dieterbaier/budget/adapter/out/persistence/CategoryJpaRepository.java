package eu.dieterbaier.budget.adapter.out.persistence;

import eu.dieterbaier.budget.adapter.out.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {
}
