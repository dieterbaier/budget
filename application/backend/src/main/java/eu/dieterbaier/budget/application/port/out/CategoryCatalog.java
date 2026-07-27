package eu.dieterbaier.budget.application.port.out;

import eu.dieterbaier.budget.domain.model.Category;
import eu.dieterbaier.budget.domain.model.CategoryGroup;

import java.util.List;

/**
 * Outbound port for maintaining categories. Separate from
 * {@link CategoryRepository}, which stays the narrow lookup the
 * transaction-recording use case needs: widening that one would give every
 * caller of it the power to delete master data.
 */
public interface CategoryCatalog {

    List<Category> findAll();

    List<Category> findInGroup(CategoryGroup group);

    void save(Category category);

    /** Replaces the category identified by {@code currentName}. */
    void rename(String currentName, Category renamed);

    void deleteByName(String name);
}
