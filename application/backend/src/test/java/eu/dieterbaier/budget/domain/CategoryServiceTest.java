package eu.dieterbaier.budget.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import eu.dieterbaier.budget.application.port.in.DuplicateNameException;
import eu.dieterbaier.budget.application.port.in.NameInUseException;
import eu.dieterbaier.budget.application.port.in.UnknownNameException;
import eu.dieterbaier.budget.application.port.out.CategoryCatalog;
import eu.dieterbaier.budget.application.port.out.CategoryGroupRepository;
import eu.dieterbaier.budget.application.port.out.CategoryRepository;
import eu.dieterbaier.budget.application.port.out.CategoryUsage;
import eu.dieterbaier.budget.application.service.CategoryGroupService;
import eu.dieterbaier.budget.application.service.CategoryService;
import eu.dieterbaier.budget.domain.model.Category;
import eu.dieterbaier.budget.domain.model.CategoryGroup;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The rules a user actually hits, at the seam where they are enforced. The
 * Cucumber feature describes the behaviour; these cover the edges it should not
 * be cluttered with — plural wording, renaming to the name already held, and the
 * unknown-name paths (ADR-018).
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    private static final CategoryGroup HOUSE = new CategoryGroup("House");

    @Mock
    private CategoryCatalog catalog;
    @Mock
    private CategoryRepository categories;
    @Mock
    private CategoryGroupRepository groups;
    @Mock
    private CategoryUsage usage;

    private CategoryService service;
    private CategoryGroupService groupService;

    @BeforeEach
    void setUp() {
        service = new CategoryService(catalog, categories, groups, usage);
        groupService = new CategoryGroupService(groups, usage);
    }

    @Test
    void rejectsACategoryInAnUnknownGroup() {
        given(groups.findByName("Nope")).willReturn(Optional.empty());

        assertThatExceptionOfType(UnknownNameException.class)
                .isThrownBy(() -> service.create("Groceries", "Nope", true))
                .withMessageContaining("category group");
    }

    @Test
    void rejectsRenamingACategoryThatDoesNotExist() {
        given(categories.findByName("Nope")).willReturn(Optional.empty());

        assertThatExceptionOfType(UnknownNameException.class)
                .isThrownBy(() -> service.update("Nope", "Something", "House", true));
    }

    // Correcting only the pension flag or the group means sending the name back
    // unchanged. That must not read as a clash with itself.
    @Test
    void allowsAnUpdateThatKeepsTheSameName() {
        Category groceries = Category.in(HOUSE, "Groceries");
        given(categories.findByName("Groceries")).willReturn(Optional.of(groceries));
        given(groups.findByName("House")).willReturn(Optional.of(HOUSE));

        Category updated = service.update("Groceries", "Groceries", "House", false);

        assertThat(updated.pensionRelevant()).isFalse();
        verify(catalog).rename("Groceries", updated);
    }

    @Test
    void rejectsRenamingOntoAnExistingName() {
        given(categories.findByName("Fuel")).willReturn(Optional.of(Category.in(HOUSE, "Fuel")));
        given(categories.findByName("Groceries")).willReturn(Optional.of(Category.in(HOUSE, "Groceries")));
        given(groups.findByName("House")).willReturn(Optional.of(HOUSE));

        assertThatExceptionOfType(DuplicateNameException.class)
                .isThrownBy(() -> service.update("Fuel", "Groceries", "House", true));

        verify(catalog, never()).rename(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void countsBothTransactionsAndFixedCostsBeforeRefusingADeletion() {
        given(categories.findByName("Groceries")).willReturn(Optional.of(Category.in(HOUSE, "Groceries")));
        given(usage.countTransactionsIn("Groceries")).willReturn(1L);
        given(usage.countFixedCostsIn("Groceries")).willReturn(2L);

        // Singular and plural in one message, and both counts, so the owner
        // learns everything they have to move in one go.
        assertThatExceptionOfType(NameInUseException.class)
                .isThrownBy(() -> service.delete("Groceries"))
                .withMessageContaining("1 transaction and 2 fixed costs");
    }

    @Test
    void refusesADeletionForFixedCostsAlone() {
        given(categories.findByName("Groceries")).willReturn(Optional.of(Category.in(HOUSE, "Groceries")));
        given(usage.countTransactionsIn("Groceries")).willReturn(0L);
        given(usage.countFixedCostsIn("Groceries")).willReturn(1L);

        assertThatExceptionOfType(NameInUseException.class)
                .isThrownBy(() -> service.delete("Groceries"))
                .withMessageContaining("0 transactions and 1 fixed cost");
    }

    @Test
    void rejectsRenamingAGroupOntoAnExistingName() {
        given(groups.findByName("Car")).willReturn(Optional.of(new CategoryGroup("Car")));
        given(groups.findByName("House")).willReturn(Optional.of(HOUSE));

        assertThatExceptionOfType(DuplicateNameException.class)
                .isThrownBy(() -> groupService.rename("Car", "House"));
    }

    @Test
    void allowsRenamingAGroupToTheNameItAlreadyHas() {
        given(groups.findByName("House")).willReturn(Optional.of(HOUSE));

        assertThat(groupService.rename("House", "House")).isEqualTo(HOUSE);
    }

    @Test
    void refusesToDeleteAGroupHoldingASingleCategory() {
        given(groups.findByName("House")).willReturn(Optional.of(HOUSE));
        given(usage.countCategoriesInGroup("House")).willReturn(1L);

        assertThatExceptionOfType(NameInUseException.class)
                .isThrownBy(() -> groupService.delete("House"))
                .withMessageContaining("1 category;");
    }

    @Test
    void rejectsAnUnknownGroupOnDeletion() {
        given(groups.findByName("Nope")).willReturn(Optional.empty());

        assertThatExceptionOfType(UnknownNameException.class)
                .isThrownBy(() -> groupService.delete("Nope"));
    }
}
