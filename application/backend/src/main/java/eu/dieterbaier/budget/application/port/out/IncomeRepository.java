package eu.dieterbaier.budget.application.port.out;

import eu.dieterbaier.budget.domain.model.Money;

/** Outbound port for the average monthly income used by the overspending signal. */
public interface IncomeRepository {

    Money averageMonthlyIncome();
}
