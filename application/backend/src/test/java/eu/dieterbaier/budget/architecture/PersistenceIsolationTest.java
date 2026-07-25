package eu.dieterbaier.budget.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTag;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * CON-003: persistence stays inside the outbound adapter. Enforces the domain
 * identity rule of ADR-009 — surrogate keys live in the JPA entities and never
 * become the domain's notion of identity — and the port-behind-a-datastore shape
 * of ADR-006.
 */
@ArchTag("architecture")
@AnalyzeClasses(packages = "eu.dieterbaier.budget", importOptions = ImportOption.DoNotIncludeTests.class)
class PersistenceIsolationTest {

    @ArchTest
    static final ArchRule jpa_entities_live_only_in_the_persistence_adapter = classes()
            .that().areAnnotatedWith("jakarta.persistence.Entity")
            .should().resideInAPackage("..budget.adapter.out.persistence.entity..")
            .because("ADR-009: persistence surrogate keys stay inside the JPA entities and never "
                    + "appear in the domain model");

    @ArchTest
    static final ArchRule spring_data_repositories_live_only_in_the_persistence_adapter = classes()
            .that().areAssignableTo("org.springframework.data.repository.Repository")
            .should().resideInAPackage("..budget.adapter.out.persistence..")
            .because("ADR-006: PostgreSQL is reached through an outbound adapter, not from the core");

    /**
     * Not covered by the layer rule: a controller returning a JPA entity is an
     * adapter reaching into another adapter, which layering permits. The local
     * data seeder is the one deliberate exception — it writes sample rows
     * directly under the {@code local} profile and never ships in a request path.
     */
    @ArchTest
    static final ArchRule jpa_entities_do_not_escape_the_persistence_adapter = noClasses()
            .that().resideOutsideOfPackages("..budget.adapter.out.persistence..", "..budget.dev..")
            .should().dependOnClassesThat().resideInAPackage("..budget.adapter.out.persistence.entity..")
            .because("ADR-009: the JPA entities are a persistence detail; the domain model is what "
                    + "crosses the port");
}
