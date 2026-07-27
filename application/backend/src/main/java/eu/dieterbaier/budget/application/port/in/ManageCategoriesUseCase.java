package eu.dieterbaier.budget.application.port.in;

import eu.dieterbaier.budget.domain.model.Category;

import java.util.List;

/** Create, rename, regroup, list and delete categories. */
public interface ManageCategoriesUseCase {

    List<Category> list();

    Category create(String name, String groupName, boolean pensionRelevant);

    /**
     * Applies every editable attribute at once. A rename and a regrouping are the
     * same kind of change to the owner — "this category is not quite right" — and
     * splitting them into separate calls would make the UI do two round trips to
     * fix one mistake.
     */
    Category update(String currentName, String newName, String groupName, boolean pensionRelevant);

    void delete(String name);
}
