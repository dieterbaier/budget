package eu.dieterbaier.budget.domain.model;

/**
 * The cycle on which a {@link Fixkostenposition} falls due. The {@code Monatsanteil}
 * amortizes the per-cycle amount to a monthly average, which is how a
 * {@code Fixkostenposition} enters the current monthly expenditure view (e.g. a
 * yearly 1200 EUR cost counts as 100 EUR/month).
 */
public enum Zahlungsintervall {
    MONTHLY(1),
    QUARTERLY(3),
    HALF_YEARLY(6),
    YEARLY(12);

    private final int monthsPerCycle;

    Zahlungsintervall(int monthsPerCycle) {
        this.monthsPerCycle = monthsPerCycle;
    }

    public int monthsPerCycle() {
        return monthsPerCycle;
    }

    public Money monatsanteil(Money amountPerCycle) {
        return amountPerCycle.dividedBy(monthsPerCycle);
    }
}
