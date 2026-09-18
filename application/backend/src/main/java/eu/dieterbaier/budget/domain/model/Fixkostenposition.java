package eu.dieterbaier.budget.domain.model;

import java.time.LocalDate;

/**
 * A recurring cost, defined once and never booked, with an amount per cycle, a
 * {@link Zahlungsintervall}, and the {@code letzteZahlung}. That date moves forward
 * each time the cost is paid; the next payment date is derived from it. Its
 * {@code Monatsanteil} feeds the current monthly expenditure view.
 */
public record Fixkostenposition(String name, Money amount, Zahlungsintervall zahlungsintervall, Category category,
                                LocalDate letzteZahlung) {

    public Money monatsanteil() {
        return zahlungsintervall.monatsanteil(amount);
    }
}
