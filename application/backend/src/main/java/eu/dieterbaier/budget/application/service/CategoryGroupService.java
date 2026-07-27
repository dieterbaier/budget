package eu.dieterbaier.budget.application.service;

import eu.dieterbaier.budget.application.port.in.DuplicateNameException;
import eu.dieterbaier.budget.application.port.in.ManageCategoryGroupsUseCase;
import eu.dieterbaier.budget.application.port.in.NameInUseException;
import eu.dieterbaier.budget.application.port.in.UnknownNameException;
import eu.dieterbaier.budget.application.port.out.CategoryGroupRepository;
import eu.dieterbaier.budget.application.port.out.CategoryUsage;
import eu.dieterbaier.budget.domain.model.CategoryGroup;

import java.util.List;

/**
 * Maintains category groups. Every rule the owner can hit is enforced here and
 * produces the message they read; no branch in this class depends on a
 * persistence exception (ADR-021).
 *
 * <p>Framework-free; wired in UseCaseConfig.
 */
public class CategoryGroupService implements ManageCategoryGroupsUseCase {

    private final CategoryGroupRepository groups;
    private final CategoryUsage usage;

    public CategoryGroupService(CategoryGroupRepository groups, CategoryUsage usage) {
        this.groups = groups;
        this.usage = usage;
    }

    @Override
    public List<CategoryGroup> list() {
        return groups.findAll();
    }

    @Override
    public CategoryGroup create(String name) {
        CategoryGroup group = new CategoryGroup(name);
        requireNameFree(group.name());
        groups.save(group);
        return group;
    }

    @Override
    public CategoryGroup rename(String currentName, String newName) {
        CategoryGroup existing = require(currentName);
        CategoryGroup renamed = existing.renamedTo(newName);

        // Renaming to the name it already has is a no-op rather than a clash with
        // itself -- the owner correcting only the capitalisation should not be
        // told the name is taken.
        if (!renamed.name().equals(existing.name())) {
            requireNameFree(renamed.name());
        }

        groups.rename(existing.name(), renamed);
        return renamed;
    }

    @Override
    public void delete(String name) {
        CategoryGroup group = require(name);

        long categories = usage.countCategoriesInGroup(group.name());
        if (categories > 0) {
            throw new NameInUseException(
                    "\"%s\" still holds %d categor%s; move them to another group first"
                            .formatted(group.name(), categories, categories == 1 ? "y" : "ies"));
        }

        groups.deleteByName(group.name());
    }

    private CategoryGroup require(String name) {
        return groups.findByName(name)
                .orElseThrow(() -> new UnknownNameException("category group", name));
    }

    private void requireNameFree(String name) {
        if (groups.findByName(name).isPresent()) {
            throw new DuplicateNameException("category group", name);
        }
    }
}
