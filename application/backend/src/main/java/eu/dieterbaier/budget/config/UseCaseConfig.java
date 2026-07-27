package eu.dieterbaier.budget.config;

import eu.dieterbaier.budget.application.port.in.GetMonthlyExpenditureUseCase;
import eu.dieterbaier.budget.application.port.in.ManageCategoriesUseCase;
import eu.dieterbaier.budget.application.port.in.ManageCategoryGroupsUseCase;
import eu.dieterbaier.budget.application.port.in.RecordTransactionUseCase;
import eu.dieterbaier.budget.application.port.out.CategoryCatalog;
import eu.dieterbaier.budget.application.port.out.CategoryGroupRepository;
import eu.dieterbaier.budget.application.port.out.CategoryRepository;
import eu.dieterbaier.budget.application.port.out.CategoryUsage;
import eu.dieterbaier.budget.application.port.out.FixedCostRepository;
import eu.dieterbaier.budget.application.port.out.IncomeRepository;
import eu.dieterbaier.budget.application.port.out.TransactionRepository;
import eu.dieterbaier.budget.application.service.CategoryGroupService;
import eu.dieterbaier.budget.application.service.CategoryService;
import eu.dieterbaier.budget.application.service.MonthlyExpenditureService;
import eu.dieterbaier.budget.application.service.RecordTransactionService;
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

    @Bean
    public RecordTransactionUseCase recordTransactionUseCase(
            CategoryRepository categoryRepository,
            TransactionRepository transactionRepository) {
        return new RecordTransactionService(categoryRepository, transactionRepository);
    }

    @Bean
    public ManageCategoryGroupsUseCase manageCategoryGroupsUseCase(
            CategoryGroupRepository categoryGroupRepository,
            CategoryUsage categoryUsage) {
        return new CategoryGroupService(categoryGroupRepository, categoryUsage);
    }

    @Bean
    public ManageCategoriesUseCase manageCategoriesUseCase(
            CategoryCatalog categoryCatalog,
            CategoryRepository categoryRepository,
            CategoryGroupRepository categoryGroupRepository,
            CategoryUsage categoryUsage) {
        return new CategoryService(
                categoryCatalog, categoryRepository, categoryGroupRepository, categoryUsage);
    }
}
