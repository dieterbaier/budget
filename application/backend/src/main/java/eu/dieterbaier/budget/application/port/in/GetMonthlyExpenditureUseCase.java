package eu.dieterbaier.budget.application.port.in;

import eu.dieterbaier.budget.domain.service.MonthlyExpenditure;

import java.time.YearMonth;

/** Inbound port: query the current monthly expenditure for a given month. */
public interface GetMonthlyExpenditureUseCase {

    MonthlyExpenditure forMonth(YearMonth month);
}
