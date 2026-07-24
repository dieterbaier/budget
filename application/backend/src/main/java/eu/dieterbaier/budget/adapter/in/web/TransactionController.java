package eu.dieterbaier.budget.adapter.in.web;

import eu.dieterbaier.budget.application.port.in.RecordTransactionUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inbound HTTP adapter for recording transactions. Depends only on the inbound
 * use-case port.
 */
@RestController
@RequestMapping("/api")
public class TransactionController {

    private final RecordTransactionUseCase recordTransaction;

    public TransactionController(RecordTransactionUseCase recordTransaction) {
        this.recordTransaction = recordTransaction;
    }

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse record(@Valid @RequestBody RecordTransactionRequest request) {
        return TransactionResponse.from(recordTransaction.record(request.toCommand()));
    }
}
