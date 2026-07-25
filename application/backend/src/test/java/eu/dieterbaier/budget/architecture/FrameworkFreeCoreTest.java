package eu.dieterbaier.budget.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTag;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * CON-001: the domain and application layers stay free of framework code, so the
 * money rules can be built with {@code new} in a plain-Java test. Enforces
 * ADR-012 and the framework-free core of ADR-004.
 */
@ArchTag("architecture")
@AnalyzeClasses(packages = "eu.dieterbaier.budget", importOptions = ImportOption.DoNotIncludeTests.class)
class FrameworkFreeCoreTest {

    private static final String[] CORE_PACKAGES = {
        "..budget.domain..",
        "..budget.application..",
    };

    /**
     * Spring and Jakarta are the ones ADR-012 names. Hibernate and Jackson are
     * listed too because they are what usually arrives next, smuggled in by a
     * persistence or serialization convenience on a domain type.
     */
    private static final String[] FRAMEWORK_PACKAGES = {
        "org.springframework..",
        "jakarta..",
        "javax..",
        "org.hibernate..",
        "com.fasterxml.jackson..",
    };

    @ArchTest
    static final ArchRule the_core_has_no_compile_time_framework_dependency = noClasses()
            .that().resideInAnyPackage(CORE_PACKAGES)
            .should().dependOnClassesThat().resideInAnyPackage(FRAMEWORK_PACKAGES)
            .because("ADR-012: the domain and application layers carry no compile-time framework "
                    + "dependency, so the core has zero framework imports");

    /**
     * Redundant with the dependency rule on its own, but it is the rule that
     * fails first and most clearly for the mistake ADR-012 actually worries
     * about: a stereotype landing on an application service.
     */
    @ArchTest
    static final ArchRule the_core_carries_no_framework_annotations = noClasses()
            .that().resideInAnyPackage(CORE_PACKAGES)
            .should().beAnnotatedWith(aFrameworkAnnotation())
            .because("ADR-012: an annotation on a core class makes @Transactional, @Value or field "
                    + "injection the natural next step, and those do cost testability");

    private static DescribedPredicate<JavaAnnotation<?>> aFrameworkAnnotation() {
        return DescribedPredicate.describe("a Spring or Jakarta annotation", annotation -> {
            String name = annotation.getRawType().getName();
            return name.startsWith("org.springframework.") || name.startsWith("jakarta.");
        });
    }
}
