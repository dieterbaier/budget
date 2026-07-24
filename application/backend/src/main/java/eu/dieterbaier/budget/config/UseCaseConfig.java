package eu.dieterbaier.budget.config;

import eu.dieterbaier.budget.application.port.in.GetMonthlyExpenditureUseCase;
import eu.dieterbaier.budget.application.port.out.FixedCostRepository;
import eu.dieterbaier.budget.application.port.out.IncomeRepository;
import eu.dieterbaier.budget.application.port.out.TransactionRepository;
import eu.dieterbaier.budget.application.service.MonthlyExpenditureService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the framework-free application services to their outbound ports. Keeping
 * this here lets the application and domain layers stay free of Spring
 * annotations; only the adapters and this configuration know about the framework.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public GetMonthlyExpenditureUseCase getMonthlyExpenditureUseCase(
            TransactionRepository transactionRepository,
            FixedCostRepository fixedCostRepository,
            IncomeRepository incomeRepository) {
        return new MonthlyExpenditureService(transactionRepository, fixedCostRepository, incomeRepository);
    }
}
