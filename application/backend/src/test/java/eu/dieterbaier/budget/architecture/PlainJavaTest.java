package eu.dieterbaier.budget.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTag;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * CON-004: the backend is plain Java — no annotation processor generates code,
 * and dependencies arrive through constructors. Enforces ADR-011 and the
 * testability driver of ADR-012.
 */
@ArchTag("architecture")
@AnalyzeClasses(packages = "eu.dieterbaier.budget", importOptions = ImportOption.DoNotIncludeTests.class)
class PlainJavaTest {

    /**
     * Lombok is absent from the dependency list today, so this rule is really a
     * tripwire: it fails the moment someone adds the dependency and the first
     * {@code @Data}, rather than after the annotations have spread.
     */
    @ArchTest
    static final ArchRule no_class_uses_lombok = noClasses()
            .should().dependOnClassesThat().resideInAnyPackage("lombok..")
            .because("ADR-011: the code is plain Java — records plus explicit code — so what is "
                    + "read is what is compiled");

    @ArchTest
    static final ArchRule no_class_uses_field_injection = NO_CLASSES_SHOULD_USE_FIELD_INJECTION
            .because("ADR-012: constructor injection is what keeps every class buildable with "
                    + "`new` in a test");
}
