package eu.dieterbaier.budget.architecture;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTag;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * CON-002: dependencies point inwards and infrastructure is reached only through
 * ports. Enforces the hexagonal layout of ADR-004 and the API boundary of
 * ADR-001.
 */
@ArchTag("architecture")
@AnalyzeClasses(packages = "eu.dieterbaier.budget", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    /**
     * {@code BudgetApplication} sits in the root package and belongs to no layer;
     * {@code consideringOnlyDependenciesInLayers} ignores it rather than forcing a
     * layer to be invented for the boot class.
     */
    @ArchTest
    static final ArchRule dependencies_point_inwards = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Domain").definedBy("..budget.domain..")
            .layer("Application").definedBy("..budget.application..")
            .layer("Adapters").definedBy("..budget.adapter..")
            .layer("Configuration").definedBy("..budget.config..")
            .layer("Development").definedBy("..budget.dev..")
            .whereLayer("Domain")
            .mayOnlyBeAccessedByLayers("Application", "Adapters", "Configuration", "Development")
            .whereLayer("Application")
            .mayOnlyBeAccessedByLayers("Adapters", "Configuration", "Development")
            .whereLayer("Adapters").mayOnlyBeAccessedByLayers("Development")
            .whereLayer("Configuration").mayNotBeAccessedByAnyLayer()
            .whereLayer("Development").mayNotBeAccessedByAnyLayer()
            .because("ADR-004: the domain knows nothing outside itself, the application layer knows "
                    + "only the domain, and infrastructure sits at arm's length behind ports");

    @ArchTest
    static final ArchRule outbound_ports_are_interfaces = classes()
            .that().resideInAPackage("..budget.application.port.out..")
            .should().beInterfaces()
            .because("ADR-004: every infrastructure interaction is reached through a port that an "
                    + "outbound adapter implements, so the datastore stays swappable");

    /**
     * Application exceptions are excluded: they are part of the contract an
     * inbound adapter maps to a status code, not a use-case implementation. That
     * they currently live in {@code application.service} rather than
     * {@code application.port.in} is what forces the exclusion.
     */
    @ArchTest
    static final ArchRule adapters_use_ports_not_use_case_implementations = noClasses()
            .that().resideInAPackage("..budget.adapter..")
            .should().dependOnClassesThat(
                    resideInAPackage("..budget.application.service..")
                            .and(not(assignableTo(Throwable.class))))
            .because("ADR-012: adapters receive use cases through their port interfaces; only "
                    + "UseCaseConfig knows which implementation sits behind a port");

    @ArchTest
    static final ArchRule http_controllers_live_in_the_inbound_web_adapter = classes()
            .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
            .should().resideInAPackage("..budget.adapter.in.web..")
            .because("ADR-001: the versioned HTTP JSON API is the driving adapter and the only "
                    + "client boundary");
}
