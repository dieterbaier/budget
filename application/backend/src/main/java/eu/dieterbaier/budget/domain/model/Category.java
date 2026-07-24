package eu.dieterbaier.budget.domain.model;

/**
 * A user-defined classification for transactions. {@code pensionRelevant} marks
 * whether spending in this category still applies in retirement (used by the
 * pension projection; e.g. a mortgage repayment is not relevant).
 */
public record Category(String name, boolean pensionRelevant) {

    public static Category of(String name) {
        return new Category(name, true);
    }
}
