package eu.dieterbaier.budget.application.port.out;

import eu.dieterbaier.budget.domain.model.Category;

import java.util.Optional;

/** Outbound port for looking up categories (master data). */
public interface CategoryRepository {

    Optional<Category> findByName(String name);
}
