package eu.dieterbaier.budget.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import eu.dieterbaier.budget.domain.model.Category;
import eu.dieterbaier.budget.domain.model.CategoryGroup;
import org.junit.jupiter.api.Test;

class CategoryTest {

    private static final CategoryGroup HOUSE = new CategoryGroup("House");
    private static final CategoryGroup CAR = new CategoryGroup("Car");

    @Test
    void isPensionRelevantByDefault() {
        // The projection counts a cost unless told otherwise, so the safe default
        // is to include it rather than silently drop it from the forecast.
        assertThat(Category.in(HOUSE, "Groceries").pensionRelevant()).isTrue();
    }

    @Test
    void rejectsAMissingName() {
        assertThatNullPointerException().isThrownBy(() -> new Category(null, HOUSE, true));
    }

    @Test
    void rejectsABlankName() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Category("  ", HOUSE, true));
    }

    @Test
    void rejectsAMissingGroup() {
        assertThatNullPointerException().isThrownBy(() -> new Category("Groceries", null, true));
    }

    @Test
    void trimsSurroundingWhitespace() {
        assertThat(Category.in(HOUSE, "  Groceries ").name()).isEqualTo("Groceries");
    }

    @Test
    void comparesByNameGroupAndRelevance() {
        assertThat(Category.in(HOUSE, "Groceries")).isEqualTo(Category.in(HOUSE, "Groceries"));
        assertThat(Category.in(HOUSE, "Groceries")).isNotEqualTo(Category.in(CAR, "Groceries"));
    }

    @Test
    void renamingKeepsTheGroupAndTheRelevanceFlag() {
        Category renamed = new Category("Grocries", HOUSE, false).renamedTo("Groceries");

        assertThat(renamed.name()).isEqualTo("Groceries");
        assertThat(renamed.group()).isEqualTo(HOUSE);
        assertThat(renamed.pensionRelevant()).isFalse();
    }

    @Test
    void movingToAnotherGroupKeepsTheNameAndTheRelevanceFlag() {
        Category moved = new Category("Fuel", HOUSE, false).movedTo(CAR);

        assertThat(moved.name()).isEqualTo("Fuel");
        assertThat(moved.group()).isEqualTo(CAR);
        assertThat(moved.pensionRelevant()).isFalse();
    }

    @Test
    void pensionRelevanceCanBeTurnedOffAndOn() {
        Category groceries = Category.in(HOUSE, "Groceries");

        assertThat(groceries.withPensionRelevance(false).pensionRelevant()).isFalse();
        assertThat(groceries.withPensionRelevance(false).withPensionRelevance(true).pensionRelevant())
                .isTrue();
    }

    @Test
    void knowsWhichGroupItIsIn() {
        assertThat(Category.in(HOUSE, "Groceries").isIn(HOUSE)).isTrue();
        assertThat(Category.in(HOUSE, "Groceries").isIn(CAR)).isFalse();
    }
}
