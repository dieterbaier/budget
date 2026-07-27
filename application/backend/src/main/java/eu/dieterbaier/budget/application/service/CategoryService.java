package eu.dieterbaier.budget.application.service;

import eu.dieterbaier.budget.application.port.in.DuplicateNameException;
import eu.dieterbaier.budget.application.port.in.ManageCategoriesUseCase;
import eu.dieterbaier.budget.application.port.in.NameInUseException;
import eu.dieterbaier.budget.application.port.in.UnknownNameException;
import eu.dieterbaier.budget.application.port.out.CategoryCatalog;
import eu.dieterbaier.budget.application.port.out.CategoryGroupRepository;
import eu.dieterbaier.budget.application.port.out.CategoryRepository;
import eu.dieterbaier.budget.application.port.out.CategoryUsage;
import eu.dieterbaier.budget.domain.model.Category;
import eu.dieterbaier.budget.domain.model.CategoryGroup;

import java.util.List;

/**
 * Maintains categories. The rules that span aggregates live here rather than in
 * the domain model, because a category cannot know how many transactions point
 * at it (ADR-021).
 *
 * <p>Framework-free; wired in UseCaseConfig.
 */
public class CategoryService implements ManageCategoriesUseCase {

    private final CategoryCatalog catalog;
    private final CategoryRepository categories;
    private final CategoryGroupRepository groups;
    private final CategoryUsage usage;

    public CategoryService(
            CategoryCatalog catalog,
            CategoryRepository categories,
            CategoryGroupRepository groups,
            CategoryUsage usage) {
        this.catalog = catalog;
        this.categories = categories;
        this.groups = groups;
        this.usage = usage;
    }

    @Override
    public List<Category> list() {
        return catalog.findAll();
    }

    @Override
    public Category create(String name, String groupName, boolean pensionRelevant) {
        Category category = new Category(name, requireGroup(groupName), pensionRelevant);
        requireNameFree(category.name());
        catalog.save(category);
        return category;
    }

    @Override
    public Category update(String currentName, String newName, String groupName, boolean pensionRelevant) {
        Category existing = require(currentName);
        Category updated = existing
                .renamedTo(newName)
                .movedTo(requireGroup(groupName))
                .withPensionRelevance(pensionRelevant);

        if (!updated.name().equals(existing.name())) {
            requireNameFree(updated.name());
        }

        catalog.rename(existing.name(), updated);
        return updated;
    }

    @Override
    public void delete(String name) {
        Category category = require(name);

        long transactions = usage.countTransactionsIn(category.name());
        long fixedCosts = usage.countFixedCostsIn(category.name());

        // Both are counted before reporting, so the owner learns everything they
        // have to move in one go rather than fixing the transactions and then
        // being told about the fixed costs.
        if (transactions > 0 || fixedCosts > 0) {
            throw new NameInUseException(
                    "\"%s\" is still used by %d transaction%s and %d fixed cost%s; recategorize them first"
                            .formatted(
                                    category.name(),
                                    transactions, transactions == 1 ? "" : "s",
                                    fixedCosts, fixedCosts == 1 ? "" : "s"));
        }

        catalog.deleteByName(category.name());
    }

    private Category require(String name) {
        return categories.findByName(name)
                .orElseThrow(() -> new UnknownNameException("category", name));
    }

    private CategoryGroup requireGroup(String name) {
        return groups.findByName(name)
                .orElseThrow(() -> new UnknownNameException("category group", name));
    }

    private void requireNameFree(String name) {
        if (categories.findByName(name).isPresent()) {
            throw new DuplicateNameException("category", name);
        }
    }
}
