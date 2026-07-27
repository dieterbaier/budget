package eu.dieterbaier.budget.adapter.out.persistence;

import eu.dieterbaier.budget.adapter.out.persistence.entity.CategoryEntity;
import eu.dieterbaier.budget.application.port.out.CategoryCatalog;
import eu.dieterbaier.budget.domain.model.Category;
import eu.dieterbaier.budget.domain.model.CategoryGroup;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Implements the catalogue port on JPA. As with groups, an update mutates the
 * existing row so that every transaction and fixed cost keeps pointing at the
 * same category across a rename (ADR-021).
 */
@Repository
public class CategoryCatalogAdapter implements CategoryCatalog {

    private final CategoryJpaRepository categories;
    private final CategoryGroupJpaRepository groups;

    public CategoryCatalogAdapter(CategoryJpaRepository categories, CategoryGroupJpaRepository groups) {
        this.categories = categories;
        this.groups = groups;
    }

    @Override
    public List<Category> findAll() {
        return categories.findAllByOrderByNameAsc().stream()
                .map(CategoryCatalogAdapter::toDomain)
                .toList();
    }

    @Override
    public List<Category> findInGroup(CategoryGroup group) {
        return categories.findByGroupNameOrderByNameAsc(group.name()).stream()
                .map(CategoryCatalogAdapter::toDomain)
                .toList();
    }

    @Override
    public void save(Category category) {
        categories.save(new CategoryEntity(
                category.name(),
                CategoryGroupPersistenceAdapter.entityFor(groups, category.group()),
                category.pensionRelevant()));
    }

    @Override
    public void rename(String currentName, Category updated) {
        CategoryEntity entity = categories.findByName(currentName)
                .orElseThrow(() -> new IllegalStateException(
                        "Category vanished between check and update: " + currentName));
        entity.update(
                updated.name(),
                CategoryGroupPersistenceAdapter.entityFor(groups, updated.group()),
                updated.pensionRelevant());
        categories.save(entity);
    }

    @Override
    public void deleteByName(String name) {
        categories.findByName(name).ifPresent(categories::delete);
    }

    static Category toDomain(CategoryEntity entity) {
        return new Category(
                entity.getName(),
                new CategoryGroup(entity.getGroup().getName()),
                entity.isPensionRelevant());
    }
}
