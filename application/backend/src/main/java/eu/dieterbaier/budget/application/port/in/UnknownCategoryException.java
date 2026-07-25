package eu.dieterbaier.budget.application.port.in;

/**
 * Raised when a command references a category that does not exist. It belongs to
 * the inbound port rather than to the service that throws it: a caller has to
 * handle it, so it is part of the use case's contract, and the web adapter maps
 * it to a status code without knowing which implementation raised it.
 */
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
