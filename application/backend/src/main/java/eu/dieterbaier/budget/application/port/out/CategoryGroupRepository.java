package eu.dieterbaier.budget.application.port.out;

import eu.dieterbaier.budget.domain.model.CategoryGroup;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for category groups, keyed by name because the name is the
 * identity (ADR-009). That keying is what makes uniqueness structural rather
 * than a rule someone has to remember to check (ADR-021).
 */
public interface CategoryGroupRepository {

    Optional<CategoryGroup> findByName(String name);

    List<CategoryGroup> findAll();

    void save(CategoryGroup group);

    /** Replaces the group identified by {@code currentName}. */
    void rename(String currentName, CategoryGroup renamed);

    void deleteByName(String name);
}
