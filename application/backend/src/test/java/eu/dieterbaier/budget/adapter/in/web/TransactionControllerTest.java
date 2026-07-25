package eu.dieterbaier.budget.adapter.in.web;

import eu.dieterbaier.budget.application.port.in.RecordTransactionUseCase;
import eu.dieterbaier.budget.application.port.in.UnknownCategoryException;
import eu.dieterbaier.budget.domain.model.Category;
import eu.dieterbaier.budget.domain.model.Money;
import eu.dieterbaier.budget.domain.model.Transaction;
import eu.dieterbaier.budget.domain.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@Import(ApiExceptionHandler.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecordTransactionUseCase recordTransaction;

    @Test
    void recordsTransactionAndReturns201() throws Exception {
        given(recordTransaction.record(any())).willReturn(new Transaction(
                LocalDate.of(2026, 7, 3), Money.of("800.00"), new Category("Groceries", true), TransactionType.EXPENSE));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date":"2026-07-03","amount":800.00,"category":"Groceries","type":"EXPENSE"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {"date":"2026-07-03","amount":800.00,"category":"Groceries","type":"EXPENSE"}
                        """, true));
    }

    @Test
    void unknownCategoryReturns400() throws Exception {
        given(recordTransaction.record(any())).willThrow(new UnknownCategoryException("Nope"));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date":"2026-07-03","amount":10.00,"category":"Nope","type":"EXPENSE"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidBodyReturns400() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":10.00}
                                """))
                .andExpect(status().isBadRequest());
    }
}
