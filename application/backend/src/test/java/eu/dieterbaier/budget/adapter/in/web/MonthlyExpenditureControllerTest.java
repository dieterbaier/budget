package eu.dieterbaier.budget.adapter.in.web;

import eu.dieterbaier.budget.application.port.in.GetMonthlyExpenditureUseCase;
import eu.dieterbaier.budget.domain.model.Money;
import eu.dieterbaier.budget.domain.service.MonthlyExpenditure;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.YearMonth;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MonthlyExpenditureController.class)
class MonthlyExpenditureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetMonthlyExpenditureUseCase getMonthlyExpenditure;

    @Test
    void returnsMonthlyExpenditureAsJson() throws Exception {
        given(getMonthlyExpenditure.forMonth(YearMonth.of(2026, 7))).willReturn(new MonthlyExpenditure(
                YearMonth.of(2026, 7),
                Money.of("900.00"),
                Money.of("100.00"),
                Money.of("1000.00"),
                Money.of("950.00")));

        String expected = """
                {
                  "month": "2026-07",
                  "variableCosts": 900.00,
                  "fixedCostsMonthly": 100.00,
                  "total": 1000.00,
                  "averageIncome": 950.00,
                  "difference": 50.00,
                  "overspending": true
                }
                """;

        mockMvc.perform(get("/api/monthly-expenditure").param("month", "2026-07"))
                .andExpect(status().isOk())
                .andExpect(content().json(expected, true));
    }

    @Test
    void rejectsMalformedMonthWithBadRequest() throws Exception {
        mockMvc.perform(get("/api/monthly-expenditure").param("month", "not-a-month"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requiresMonthParameter() throws Exception {
        mockMvc.perform(get("/api/monthly-expenditure"))
                .andExpect(status().isBadRequest());
    }
}
