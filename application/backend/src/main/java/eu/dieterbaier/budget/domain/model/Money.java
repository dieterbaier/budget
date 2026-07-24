package eu.dieterbaier.budget.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Exact monetary amount in the single supported currency (EUR). Money is always
 * scaled to two decimal places; division rounds half-up. There is no
 * floating-point money anywhere in the domain (see ADR-006 / crosscutting
 * concepts).
 */
public record Money(BigDecimal amount) {

    public static final Money ZERO = Money.of("0");

    public Money {
        Objects.requireNonNull(amount, "amount");
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(String amount) {
        return new Money(new BigDecimal(amount));
    }

    public static Money of(long amount) {
        return new Money(BigDecimal.valueOf(amount));
    }

    public Money add(Money other) {
        return new Money(amount.add(other.amount));
    }

    public Money subtract(Money other) {
        return new Money(amount.subtract(other.amount));
    }

    public Money negate() {
        return new Money(amount.negate());
    }

    /** Amortizes an amount over a whole number of months (e.g. yearly / 12). */
    public Money dividedBy(int divisor) {
        return new Money(amount.divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP));
    }

    public boolean isGreaterThan(Money other) {
        return amount.compareTo(other.amount) > 0;
    }
}
