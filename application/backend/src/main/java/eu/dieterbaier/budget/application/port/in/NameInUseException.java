package eu.dieterbaier.budget.application.port.in;

/**
 * Master data cannot be deleted because something still references it. The
 * message names what is in the way, because the owner's next step is to move
 * those records somewhere else.
 */
public class NameInUseException extends RuntimeException {

    public NameInUseException(String message) {
        super(message);
    }
}
