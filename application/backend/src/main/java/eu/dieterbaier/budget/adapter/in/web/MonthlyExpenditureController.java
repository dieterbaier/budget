package eu.dieterbaier.budget.adapter.in.web;

import eu.dieterbaier.budget.application.port.in.GetMonthlyExpenditureUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

/**
 * Inbound HTTP adapter for the current monthly expenditure view. It depends only
 * on the inbound use-case port, never on the application service directly.
 */
@RestController
@RequestMapping("/api")
public class MonthlyExpenditureController {

    private final GetMonthlyExpenditureUseCase getMonthlyExpenditure;

    public MonthlyExpenditureController(GetMonthlyExpenditureUseCase getMonthlyExpenditure) {
        this.getMonthlyExpenditure = getMonthlyExpenditure;
    }

    @GetMapping("/monthly-expenditure")
    public MonthlyExpenditureResponse monthlyExpenditure(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return MonthlyExpenditureResponse.from(getMonthlyExpenditure.forMonth(month));
    }
}
