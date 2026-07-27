package eu.dieterbaier.budget.domain.model;

import java.util.Objects;

/**
 * A named grouping of categories — house, car, dog, and so on — used by the
 * spend-per-group view.
 *
 * <p>Identity is the name (ADR-009). The name is also editable, and those two
 * facts sit together deliberately: renaming a group is allowed precisely because
 * the name stays unique, so it still identifies exactly one group afterwards.
 * Nothing else in the domain holds a copy of it to keep in step.
 */
public record CategoryGroup(String name) {

    public CategoryGroup {
        Objects.requireNonNull(name, "name");
        name = name.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("A category group needs a name");
        }
    }

    /**
     * The same group under a new name. A record, so this returns a new value
     * rather than mutating: the caller replaces the group in the repository,
     * which is what makes the rename visible everywhere the group is used.
     */
    public CategoryGroup renamedTo(String newName) {
        return new CategoryGroup(newName);
    }
}
