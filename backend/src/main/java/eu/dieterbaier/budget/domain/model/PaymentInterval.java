package eu.dieterbaier.budget.domain.model;

/**
 * Payment cadence of a fixed cost. The monthly portion amortizes the per-cycle
 * amount to a monthly average, which is how fixed costs enter the current
 * monthly expenditure view (e.g. a yearly 1200 EUR cost counts as 100 EUR/month).
 */
public enum PaymentInterval {
    MONTHLY(1),
    QUARTERLY(3),
    HALF_YEARLY(6),
    YEARLY(12);

    private final int monthsPerCycle;

    PaymentInterval(int monthsPerCycle) {
        this.monthsPerCycle = monthsPerCycle;
    }

    public int monthsPerCycle() {
        return monthsPerCycle;
    }

    public Money monthlyPortion(Money amountPerCycle) {
        return amountPerCycle.dividedBy(monthsPerCycle);
    }
}
