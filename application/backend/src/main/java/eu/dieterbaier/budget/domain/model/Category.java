package eu.dieterbaier.budget.domain.model;

import java.util.Objects;

/**
 * A user-defined classification for transactions, belonging to exactly one
 * {@link CategoryGroup}. {@code pensionRelevant} marks whether spending in this
 * category still applies in retirement (used by the pension projection; e.g. a
 * mortgage repayment is not relevant).
 *
 * <p>Identity is the name (ADR-009), and the name is editable. Those hold
 * together because the name stays unique: after a rename it still identifies
 * exactly one category. Nothing in the domain keeps a second copy of the name
 * that would have to be updated in step — a transaction holds the category
 * itself, and the persistence surrogate key is what carries the reference across
 * a rename (ADR-021).
 */
public record Category(String name, CategoryGroup group, boolean pensionRelevant) {

    public Category {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(group, "group");
        name = name.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("A category needs a name");
        }
    }

    /** A pension-relevant category in the given group, which is the common case. */
    public static Category in(CategoryGroup group, String name) {
        return new Category(name, group, true);
    }

    public Category renamedTo(String newName) {
        return new Category(newName, group, pensionRelevant);
    }

    public Category movedTo(CategoryGroup newGroup) {
        return new Category(name, newGroup, pensionRelevant);
    }

    public Category withPensionRelevance(boolean relevant) {
        return new Category(name, group, relevant);
    }

    public boolean isIn(CategoryGroup other) {
        return group.equals(other);
    }
}
