package eu.dieterbaier.budget.domain;

import eu.dieterbaier.budget.domain.model.Money;
import eu.dieterbaier.budget.domain.model.Zahlungsintervall;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ZahlungsintervallTest {

    @Test
    void amortizesYearlyToOneTwelfth() {
        assertThat(Zahlungsintervall.YEARLY.monatsanteil(Money.of("1200"))).isEqualTo(Money.of("100.00"));
    }

    @Test
    void amortizesQuarterlyToOneThird() {
        assertThat(Zahlungsintervall.QUARTERLY.monatsanteil(Money.of("300"))).isEqualTo(Money.of("100.00"));
    }

    @Test
    void amortizesHalfYearlyToOneSixth() {
        assertThat(Zahlungsintervall.HALF_YEARLY.monatsanteil(Money.of("600"))).isEqualTo(Money.of("100.00"));
    }

    @Test
    void monthlyKeepsTheFullAmount() {
        assertThat(Zahlungsintervall.MONTHLY.monatsanteil(Money.of("50"))).isEqualTo(Money.of("50.00"));
    }

    @Test
    void roundsIndivisibleAmountsHalfUp() {
        // 1000 / 12 = 83.3333... -> 83.33
        assertThat(Zahlungsintervall.YEARLY.monatsanteil(Money.of("1000"))).isEqualTo(Money.of("83.33"));
    }

    @Test
    void exposesTheCycleLengthInMonths() {
        assertThat(Zahlungsintervall.MONTHLY.monthsPerCycle()).isEqualTo(1);
        assertThat(Zahlungsintervall.QUARTERLY.monthsPerCycle()).isEqualTo(3);
        assertThat(Zahlungsintervall.HALF_YEARLY.monthsPerCycle()).isEqualTo(6);
        assertThat(Zahlungsintervall.YEARLY.monthsPerCycle()).isEqualTo(12);
    }
}
