package eu.dieterbaier.budget.adapter.out.persistence;

import eu.dieterbaier.budget.adapter.out.persistence.entity.CategoryGroupEntity;
import eu.dieterbaier.budget.application.port.out.CategoryGroupRepository;
import eu.dieterbaier.budget.domain.model.CategoryGroup;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Implements the group port on JPA. {@code rename} updates the existing row
 * rather than deleting and inserting: the surrogate key stays, so every category
 * pointing at the group follows the new name without the domain doing anything
 * (ADR-021).
 */
@Repository
public class CategoryGroupPersistenceAdapter implements CategoryGroupRepository {

    private final CategoryGroupJpaRepository jpaRepository;

    public CategoryGroupPersistenceAdapter(CategoryGroupJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<CategoryGroup> findByName(String name) {
        return jpaRepository.findByName(name).map(CategoryGroupPersistenceAdapter::toDomain);
    }

    @Override
    public List<CategoryGroup> findAll() {
        return jpaRepository.findAll(org.springframework.data.domain.Sort.by("name")).stream()
                .map(CategoryGroupPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public void save(CategoryGroup group) {
        jpaRepository.save(new CategoryGroupEntity(group.name()));
    }

    @Override
    public void rename(String currentName, CategoryGroup renamed) {
        CategoryGroupEntity entity = jpaRepository.findByName(currentName)
                .orElseThrow(() -> new IllegalStateException(
                        "Category group vanished between check and rename: " + currentName));
        entity.rename(renamed.name());
        jpaRepository.save(entity);
    }

    @Override
    public void deleteByName(String name) {
        jpaRepository.findByName(name).ifPresent(jpaRepository::delete);
    }

    static CategoryGroup toDomain(CategoryGroupEntity entity) {
        return new CategoryGroup(entity.getName());
    }

    static CategoryGroupEntity entityFor(CategoryGroupJpaRepository repository, CategoryGroup group) {
        return repository.findByName(group.name())
                .orElseThrow(() -> new IllegalStateException("Unknown category group: " + group.name()));
    }
}
