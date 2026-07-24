package eu.dieterbaier.budget.application.port.in;

import eu.dieterbaier.budget.domain.model.Transaction;

/** Inbound port: record a transaction. */
public interface RecordTransactionUseCase {

    Transaction record(RecordTransactionCommand command);
}
