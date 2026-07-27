package eu.dieterbaier.budget.domain;

import eu.dieterbaier.budget.application.port.in.RecordTransactionCommand;
import eu.dieterbaier.budget.application.port.out.CategoryRepository;
import eu.dieterbaier.budget.application.port.out.TransactionRepository;
import eu.dieterbaier.budget.application.service.RecordTransactionService;
import eu.dieterbaier.budget.application.port.in.UnknownCategoryException;
import eu.dieterbaier.budget.domain.model.Category;
import eu.dieterbaier.budget.domain.model.CategoryGroup;
import eu.dieterbaier.budget.domain.model.Money;
import eu.dieterbaier.budget.domain.model.Transaction;
import eu.dieterbaier.budget.domain.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class RecordTransactionServiceTest {

    private final CategoryRepository categories = mock(CategoryRepository.class);
    private final TransactionRepository transactions = mock(TransactionRepository.class);
    private final RecordTransactionService service = new RecordTransactionService(categories, transactions);

    @Test
    void recordsTransactionForKnownCategory() {
        given(categories.findByName("Groceries")).willReturn(Optional.of(Category.in(new CategoryGroup("House"), "Groceries")));

        Transaction result = service.record(new RecordTransactionCommand(
                LocalDate.of(2026, 7, 3), new BigDecimal("800.00"), "Groceries", TransactionType.EXPENSE));

        assertThat(result.amount()).isEqualTo(Money.of("800.00"));
        assertThat(result.category().name()).isEqualTo("Groceries");
        assertThat(result.type()).isEqualTo(TransactionType.EXPENSE);

        ArgumentCaptor<Transaction> saved = ArgumentCaptor.forClass(Transaction.class);
        verify(transactions).save(saved.capture());
        assertThat(saved.getValue().amount()).isEqualTo(Money.of("800.00"));
    }

    @Test
    void rejectsUnknownCategoryAndDoesNotSave() {
        given(categories.findByName("Nope")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.record(new RecordTransactionCommand(
                LocalDate.of(2026, 7, 3), new BigDecimal("10.00"), "Nope", TransactionType.EXPENSE)))
                .isInstanceOf(UnknownCategoryException.class);

        verify(transactions, never()).save(any());
    }
}
