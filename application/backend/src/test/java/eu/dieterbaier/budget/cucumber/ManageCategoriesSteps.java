package eu.dieterbaier.budget.cucumber;

import static org.assertj.core.api.Assertions.assertThat;

import eu.dieterbaier.budget.application.port.in.DuplicateNameException;
import eu.dieterbaier.budget.application.port.in.ManageCategoriesUseCase;
import eu.dieterbaier.budget.application.port.in.ManageCategoryGroupsUseCase;
import eu.dieterbaier.budget.application.port.in.NameInUseException;
import eu.dieterbaier.budget.application.port.out.CategoryCatalog;
import eu.dieterbaier.budget.application.port.out.CategoryGroupRepository;
import eu.dieterbaier.budget.application.port.out.CategoryRepository;
import eu.dieterbaier.budget.application.port.out.CategoryUsage;
import eu.dieterbaier.budget.application.service.CategoryGroupService;
import eu.dieterbaier.budget.application.service.CategoryService;
import eu.dieterbaier.budget.domain.model.Category;
import eu.dieterbaier.budget.domain.model.CategoryGroup;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Drives the category use cases through their inbound ports, backed by in-memory
 * outbound ports — the same hexagonal seams the JPA adapters plug into.
 *
 * <p>The in-memory stores are maps keyed by name, which is not a shortcut but the
 * point: ADR-009 makes the name the identity, so a map is what the port promises
 * and a unique index is only how the relational adapter delivers it (ADR-021).
 */
public class ManageCategoriesSteps {

    private final Map<String, CategoryGroup> groups = new LinkedHashMap<>();
    private final Map<String, Category> categories = new LinkedHashMap<>();
    private final Map<String, Integer> transactionCounts = new LinkedHashMap<>();

    private final ManageCategoryGroupsUseCase manageGroups;
    private final ManageCategoriesUseCase manageCategories;

    private RuntimeException rejection;

    public ManageCategoriesSteps() {
        CategoryGroupRepository groupRepository = new CategoryGroupRepository() {
            @Override
            public Optional<CategoryGroup> findByName(String name) {
                return Optional.ofNullable(groups.get(name));
            }

            @Override
            public List<CategoryGroup> findAll() {
                return new ArrayList<>(groups.values());
            }

            @Override
            public void save(CategoryGroup group) {
                groups.put(group.name(), group);
            }

            @Override
            public void rename(String currentName, CategoryGroup renamed) {
                groups.remove(currentName);
                groups.put(renamed.name(), renamed);
                // What the surrogate key does for free in the JPA adapter has to
                // be done by hand here: every category pointing at the old group
                // now points at the renamed one.
                categories.replaceAll((name, category) ->
                        category.group().name().equals(currentName) ? category.movedTo(renamed) : category);
            }

            @Override
            public void deleteByName(String name) {
                groups.remove(name);
            }
        };

        CategoryRepository categoryLookup = name -> Optional.ofNullable(categories.get(name));

        CategoryCatalog catalog = new CategoryCatalog() {
            @Override
            public List<Category> findAll() {
                return categories.values().stream()
                        .sorted((a, b) -> a.name().compareTo(b.name()))
                        .toList();
            }

            @Override
            public List<Category> findInGroup(CategoryGroup group) {
                return categories.values().stream().filter(category -> category.isIn(group)).toList();
            }

            @Override
            public void save(Category category) {
                categories.put(category.name(), category);
            }

            @Override
            public void rename(String currentName, Category updated) {
                categories.remove(currentName);
                categories.put(updated.name(), updated);
                Integer used = transactionCounts.remove(currentName);
                if (used != null) {
                    transactionCounts.put(updated.name(), used);
                }
            }

            @Override
            public void deleteByName(String name) {
                categories.remove(name);
            }
        };

        CategoryUsage usage = new CategoryUsage() {
            @Override
            public long countTransactionsIn(String categoryName) {
                return transactionCounts.getOrDefault(categoryName, 0);
            }

            @Override
            public long countFixedCostsIn(String categoryName) {
                return 0;
            }

            @Override
            public long countCategoriesInGroup(String groupName) {
                return categories.values().stream()
                        .filter(category -> category.group().name().equals(groupName))
                        .count();
            }
        };

        manageGroups = new CategoryGroupService(groupRepository, usage);
        manageCategories = new CategoryService(catalog, categoryLookup, groupRepository, usage);
    }

    @Given("the category group {string}")
    public void theCategoryGroup(String name) {
        manageGroups.create(name);
    }

    @Given("the category {string} in {string}")
    public void theCategoryIn(String name, String group) {
        manageCategories.create(name, group, true);
    }

    @Given("a transaction of {double} EUR in category {string}")
    public void aTransactionInCategory(double amount, String category) {
        transactionCounts.merge(category, 1, Integer::sum);
    }

    @When("I add the category {string} to {string}")
    public void iAddTheCategoryTo(String name, String group) {
        capture(() -> manageCategories.create(name, group, true));
    }

    @When("I rename the category {string} to {string}")
    public void iRenameTheCategory(String currentName, String newName) {
        Category existing = categories.get(currentName);
        capture(() -> manageCategories.update(
                currentName, newName, existing.group().name(), existing.pensionRelevant()));
    }

    @When("I rename the category group {string} to {string}")
    public void iRenameTheCategoryGroup(String currentName, String newName) {
        capture(() -> manageGroups.rename(currentName, newName));
    }

    @When("I delete the category {string}")
    public void iDeleteTheCategory(String name) {
        capture(() -> manageCategories.delete(name));
    }

    @Then("the categories are:")
    public void theCategoriesAre(DataTable table) {
        List<Map<String, String>> expected = table.asMaps();
        List<Category> actual = manageCategories.list();

        assertThat(actual).hasSameSizeAs(expected);
        for (int i = 0; i < expected.size(); i++) {
            assertThat(actual.get(i).name()).isEqualTo(expected.get(i).get("name"));
            assertThat(actual.get(i).group().name()).isEqualTo(expected.get(i).get("group"));
        }
    }

    @Then("the category {string} exists")
    public void theCategoryExists(String name) {
        assertThat(categories).containsKey(name);
    }

    @Then("the category {string} is gone")
    public void theCategoryIsGone(String name) {
        assertThat(categories).doesNotContainKey(name);
    }

    @Then("the category {string} is in group {string}")
    public void theCategoryIsInGroup(String name, String group) {
        assertThat(categories.get(name).group().name()).isEqualTo(group);
    }

    @Then("{string} has {int} transaction")
    public void hasTransactions(String category, int count) {
        assertThat(transactionCounts.getOrDefault(category, 0)).isEqualTo(count);
    }

    @Then("it is rejected because the name is taken")
    public void itIsRejectedBecauseTheNameIsTaken() {
        assertThat(rejection).isInstanceOf(DuplicateNameException.class);
    }

    @Then("it is rejected because it is still in use")
    public void itIsRejectedBecauseItIsStillInUse() {
        assertThat(rejection).isInstanceOf(NameInUseException.class);
    }

    private void capture(Runnable action) {
        rejection = null;
        try {
            action.run();
        } catch (RuntimeException e) {
            rejection = e;
        }
    }
}
