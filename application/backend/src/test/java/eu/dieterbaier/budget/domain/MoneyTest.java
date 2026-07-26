package eu.dieterbaier.budget.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import eu.dieterbaier.budget.domain.model.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Money had no test of its own: it was covered only incidentally, through the
 * calculators that use it. It is the value object every figure in the
 * application passes through, so its rounding and scale rules are exactly what
 * QG-003 means by calculation correctness.
 */
class MoneyTest {

    @Test
    void scalesToTwoDecimalPlaces() {
        assertThat(Money.of("5").amount()).isEqualTo(new BigDecimal("5.00"));
    }

    @Test
    void roundsHalfUpToTheCent() {
        assertThat(Money.of("1.005").amount()).isEqualTo(new BigDecimal("1.01"));
        assertThat(Money.of("1.004").amount()).isEqualTo(new BigDecimal("1.00"));
    }

    @Test
    void acceptsWholeAmountsAsNumbers() {
        assertThat(Money.of(42L)).isEqualTo(Money.of("42.00"));
    }

    @Test
    void rejectsAMissingAmount() {
        assertThatNullPointerException().isThrownBy(() -> new Money(null));
    }

    @Test
    void zeroIsScaledLikeAnyOtherAmount() {
        assertThat(Money.ZERO).isEqualTo(Money.of("0.00"));
    }

    @Test
    void addsAndSubtracts() {
        assertThat(Money.of("10.50").add(Money.of("4.50"))).isEqualTo(Money.of("15.00"));
        assertThat(Money.of("10.50").subtract(Money.of("4.50"))).isEqualTo(Money.of("6.00"));
    }

    @Test
    void negatesInBothDirections() {
        assertThat(Money.of("10.00").negate()).isEqualTo(Money.of("-10.00"));
        assertThat(Money.of("-10.00").negate()).isEqualTo(Money.of("10.00"));
    }

    @Test
    void subtractingPastZeroGivesANegativeAmount() {
        // Refunds are expenses with a negative amount, so this is a real case
        // rather than a defensive one.
        assertThat(Money.of("10.00").subtract(Money.of("25.00"))).isEqualTo(Money.of("-15.00"));
    }

    @Test
    void dividesWithHalfUpRounding() {
        // 100 / 3 = 33.333... -> 33.33; 200 / 3 = 66.666... -> 66.67
        assertThat(Money.of("100").dividedBy(3)).isEqualTo(Money.of("33.33"));
        assertThat(Money.of("200").dividedBy(3)).isEqualTo(Money.of("66.67"));
    }

    @Test
    void comparesAmounts() {
        assertThat(Money.of("10.01").isGreaterThan(Money.of("10.00"))).isTrue();
        assertThat(Money.of("10.00").isGreaterThan(Money.of("10.01"))).isFalse();
    }

    @Test
    void isNotGreaterThanAnEqualAmount() {
        // The boundary, because overspending is decided by this comparison and
        // "equal" must not read as "over".
        assertThat(Money.of("10.00").isGreaterThan(Money.of("10.00"))).isFalse();
    }

    @Test
    void treatsDifferentlyScaledInputsAsTheSameAmount() {
        assertThat(Money.of("7.5")).isEqualTo(Money.of("7.50"));
    }
}
