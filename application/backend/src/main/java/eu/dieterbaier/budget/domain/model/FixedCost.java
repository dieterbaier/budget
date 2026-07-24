package eu.dieterbaier.budget.domain.model;

import java.time.LocalDate;

/**
 * A recurring cost with an amount per cycle, a payment interval, and an anchor
 * date from which the next payment date is derived. Its monthly portion feeds
 * the current monthly expenditure view.
 */
public record FixedCost(String name, Money amount, PaymentInterval interval, Category category, LocalDate anchorDate) {

    public Money monthlyPortion() {
        return interval.monthlyPortion(amount);
    }
}
