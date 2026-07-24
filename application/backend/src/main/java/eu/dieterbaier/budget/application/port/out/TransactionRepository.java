package eu.dieterbaier.budget.application.port.out;

import eu.dieterbaier.budget.domain.model.Transaction;

import java.time.YearMonth;
import java.util.List;

/** Outbound port for reading transactions. Implemented by a persistence adapter. */
public interface TransactionRepository {

    List<Transaction> findByMonth(YearMonth month);

    void save(Transaction transaction);
}
