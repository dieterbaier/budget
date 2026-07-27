package eu.dieterbaier.budget.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import eu.dieterbaier.budget.domain.model.CategoryGroup;
import org.junit.jupiter.api.Test;

class CategoryGroupTest {

    @Test
    void rejectsAMissingName() {
        assertThatNullPointerException().isThrownBy(() -> new CategoryGroup(null));
    }

    @Test
    void rejectsABlankName() {
        assertThatIllegalArgumentException().isThrownBy(() -> new CategoryGroup("   "));
    }

    @Test
    void trimsSurroundingWhitespace() {
        // Otherwise " House" and "House" would be two groups that look identical
        // in every list the owner ever sees.
        assertThat(new CategoryGroup("  House  ").name()).isEqualTo("House");
    }

    @Test
    void comparesByName() {
        // The name is the identity (ADR-009), so equality has to follow it.
        assertThat(new CategoryGroup("House")).isEqualTo(new CategoryGroup("House"));
        assertThat(new CategoryGroup("House")).isNotEqualTo(new CategoryGroup("Car"));
    }

    @Test
    void renamingYieldsTheGroupUnderItsNewName() {
        assertThat(new CategoryGroup("Huose").renamedTo("House")).isEqualTo(new CategoryGroup("House"));
    }
}
