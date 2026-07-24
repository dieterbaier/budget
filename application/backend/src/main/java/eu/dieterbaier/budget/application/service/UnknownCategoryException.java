package eu.dieterbaier.budget.application.service;

/** Raised when a command references a category that does not exist. */
public class UnknownCategoryException extends RuntimeException {

    private final String categoryName;

    public UnknownCategoryException(String categoryName) {
        super("Unknown category: " + categoryName);
        this.categoryName = categoryName;
    }

    public String categoryName() {
        return categoryName;
    }
}
