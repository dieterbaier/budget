package eu.dieterbaier.budget.domain;

import eu.dieterbaier.budget.domain.model.Money;
import eu.dieterbaier.budget.domain.model.PaymentInterval;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentIntervalTest {

    @Test
    void amortizesYearlyToOneTwelfth() {
        assertThat(PaymentInterval.YEARLY.monthlyPortion(Money.of("1200"))).isEqualTo(Money.of("100.00"));
    }

    @Test
    void amortizesQuarterlyToOneThird() {
        assertThat(PaymentInterval.QUARTERLY.monthlyPortion(Money.of("300"))).isEqualTo(Money.of("100.00"));
    }

    @Test
    void amortizesHalfYearlyToOneSixth() {
        assertThat(PaymentInterval.HALF_YEARLY.monthlyPortion(Money.of("600"))).isEqualTo(Money.of("100.00"));
    }

    @Test
    void monthlyKeepsTheFullAmount() {
        assertThat(PaymentInterval.MONTHLY.monthlyPortion(Money.of("50"))).isEqualTo(Money.of("50.00"));
    }

    @Test
    void roundsIndivisibleAmountsHalfUp() {
        // 1000 / 12 = 83.3333... -> 83.33
        assertThat(PaymentInterval.YEARLY.monthlyPortion(Money.of("1000"))).isEqualTo(Money.of("83.33"));
    }
}
