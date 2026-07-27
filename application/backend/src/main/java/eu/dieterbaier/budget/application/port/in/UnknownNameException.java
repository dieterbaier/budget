package eu.dieterbaier.budget.application.port.in;

/** No group or category exists under the given name. */
public class UnknownNameException extends RuntimeException {

    public UnknownNameException(String what, String name) {
        super("No %s named \"%s\"".formatted(what, name));
    }
}
