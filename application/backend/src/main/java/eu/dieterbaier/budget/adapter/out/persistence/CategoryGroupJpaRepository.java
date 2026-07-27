package eu.dieterbaier.budget.adapter.out.persistence;

import eu.dieterbaier.budget.adapter.out.persistence.entity.CategoryGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryGroupJpaRepository extends JpaRepository<CategoryGroupEntity, Long> {

    Optional<CategoryGroupEntity> findByName(String name);
}
