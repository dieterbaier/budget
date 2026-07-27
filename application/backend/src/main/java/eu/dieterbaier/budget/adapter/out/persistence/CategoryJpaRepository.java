package eu.dieterbaier.budget.adapter.out.persistence;

import eu.dieterbaier.budget.adapter.out.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {

    Optional<CategoryEntity> findByName(String name);

    List<CategoryEntity> findAllByOrderByNameAsc();

    List<CategoryEntity> findByGroupNameOrderByNameAsc(String groupName);

    long countByGroupName(String groupName);
}
