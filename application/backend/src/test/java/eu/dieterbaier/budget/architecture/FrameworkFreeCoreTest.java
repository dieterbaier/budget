package eu.dieterbaier.budget.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTag;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.util.List;

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
     *
     * <p>Both rules below derive from this one list, so a framework added here is
     * caught as a dependency <em>and</em> as an annotation. Package roots, not
     * ArchUnit identifiers, because the annotation predicate needs a plain prefix.
     */
    static final List<String> FRAMEWORK_PACKAGES = List.of(
            "org.springframework",
            "jakarta",
            "javax",
            "org.hibernate",
            "com.fasterxml.jackson");

    private static final String[] FRAMEWORK_PACKAGE_IDENTIFIERS =
            FRAMEWORK_PACKAGES.stream().map(root -> root + "..").toArray(String[]::new);

    @ArchTest
    static final ArchRule the_core_has_no_compile_time_framework_dependency = noClasses()
            .that().resideInAnyPackage(CORE_PACKAGES)
            .should().dependOnClassesThat().resideInAnyPackage(FRAMEWORK_PACKAGE_IDENTIFIERS)
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

    /**
     * Matches an annotation whose type comes from any framework in
     * {@link #FRAMEWORK_PACKAGES} — {@code @Service} and {@code @Transactional}
     * from Spring, {@code @Entity} from Jakarta, {@code @JsonProperty} from
     * Jackson. Package-private so {@link FrameworkAnnotationPredicateTest} can
     * show what it does and does not match.
     */
    static DescribedPredicate<JavaAnnotation<?>> aFrameworkAnnotation() {
        return DescribedPredicate.describe(
                "an annotation from " + String.join(", ", FRAMEWORK_PACKAGES),
                annotation -> isFrameworkAnnotationType(annotation.getRawType().getName()));
    }

    /**
     * Split out from the predicate so it can be exercised directly for every
     * framework in the list, including the ones this project does not have on the
     * classpath to write a fixture against.
     */
    static boolean isFrameworkAnnotationType(String annotationTypeName) {
        return FRAMEWORK_PACKAGES.stream().anyMatch(root -> annotationTypeName.startsWith(root + "."));
    }
}
