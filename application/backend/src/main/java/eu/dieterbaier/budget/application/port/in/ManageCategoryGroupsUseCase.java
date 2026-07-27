package eu.dieterbaier.budget.application.port.in;

import eu.dieterbaier.budget.domain.model.CategoryGroup;

import java.util.List;

/** Create, rename, list and delete the groups categories are organised into. */
public interface ManageCategoryGroupsUseCase {

    List<CategoryGroup> list();

    CategoryGroup create(String name);

    CategoryGroup rename(String currentName, String newName);

    void delete(String name);
}
