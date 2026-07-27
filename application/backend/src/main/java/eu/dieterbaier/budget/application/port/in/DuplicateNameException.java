package eu.dieterbaier.budget.application.port.in;

/**
 * The requested name is already taken. Part of the use case's contract, not an
 * implementation detail: a caller has to handle it, so it lives beside the port
 * rather than in the service package (see CON-002).
 *
 * <p>Raised by the domain-side check, never by catching a persistence
 * constraint violation (ADR-021).
 */
public class DuplicateNameException extends RuntimeException {

    public DuplicateNameException(String what, String name) {
        super("A %s named \"%s\" already exists".formatted(what, name));
    }
}
