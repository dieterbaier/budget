package eu.dieterbaier.budget.adapter.out.persistence;

import eu.dieterbaier.budget.application.port.out.CategoryRepository;
import eu.dieterbaier.budget.domain.model.Category;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Outbound adapter implementing the category repository port on top of JPA. */
@Repository
public class CategoryPersistenceAdapter implements CategoryRepository {

    private final CategoryJpaRepository jpaRepository;

    public CategoryPersistenceAdapter(CategoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Category> findByName(String name) {
        return jpaRepository.findByName(name).map(TransactionPersistenceAdapter::toDomain);
    }
}
