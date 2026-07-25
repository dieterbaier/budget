package eu.dieterbaier.budget.architecture;

import static eu.dieterbaier.budget.architecture.FrameworkFreeCoreTest.FRAMEWORK_PACKAGES;
import static eu.dieterbaier.budget.architecture.FrameworkFreeCoreTest.aFrameworkAnnotation;
import static eu.dieterbaier.budget.architecture.FrameworkFreeCoreTest.isFrameworkAnnotationType;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.stereotype.Service;

/**
 * Worked examples of what CON-001's annotation predicate treats as a framework
 * annotation, so its intent is readable without running the rule against a
 * deliberately broken core.
 *
 * <p>To ban another framework, add it to
 * {@link FrameworkFreeCoreTest#FRAMEWORK_PACKAGES} and nowhere else — that one
 * list feeds both the dependency rule and the annotation predicate. Nothing here
 * needs editing: {@link #every_framework_the_dependency_rule_lists_is_matched_as_an_annotation()}
 * iterates the list, so it covers the new entry automatically and fails if the
 * two rules ever stop sharing it.
 *
 * <p>These are plain JUnit tests, not ArchUnit rules — they check the predicate,
 * not the architecture — so they carry JUnit's {@code @Tag} rather than
 * {@code @ArchTag} and are excluded by the same
 * {@code -PexcludeTags=architecture} switch.
 */
@Tag("architecture")
class FrameworkAnnotationPredicateTest {

    /**
     * Fixtures live here rather than in a core package on purpose: a class under
     * {@code ..budget.application..} carrying {@code @Service} would be a real
     * violation waiting for someone to widen the rule's import options.
     */
    @Service
    private static final class SpringAnnotatedFixture {
    }

    @Deprecated
    private static final class JdkAnnotatedFixture {
    }

    private static final class PlainFixture {
    }

    private final JavaClasses fixtures = new ClassFileImporter()
            .importClasses(SpringAnnotatedFixture.class, JdkAnnotatedFixture.class, PlainFixture.class);

    @Test
    void a_spring_stereotype_is_a_framework_annotation() {
        assertThat(hasFrameworkAnnotation(SpringAnnotatedFixture.class)).isTrue();
    }

    @Test
    void a_jdk_annotation_is_not() {
        assertThat(hasFrameworkAnnotation(JdkAnnotatedFixture.class)).isFalse();
    }

    @Test
    void an_unannotated_class_has_none() {
        assertThat(hasFrameworkAnnotation(PlainFixture.class)).isFalse();
    }

    /**
     * The reason the predicate and the dependency rule share one list: they used
     * to drift, and an annotation from a framework the dependency rule already
     * banned went unnoticed.
     */
    @Test
    void every_framework_the_dependency_rule_lists_is_matched_as_an_annotation() {
        assertThat(FRAMEWORK_PACKAGES)
                .allSatisfy(root -> assertThat(isFrameworkAnnotationType(root + ".SomeAnnotation")).isTrue());
    }

    @Test
    void a_package_that_merely_starts_with_a_framework_name_is_not_matched() {
        assertThat(isFrameworkAnnotationType("jakartaesque.Annotation")).isFalse();
        assertThat(isFrameworkAnnotationType("eu.dieterbaier.budget.domain.model.Money")).isFalse();
    }

    private boolean hasFrameworkAnnotation(Class<?> type) {
        return fixtures.get(type).getAnnotations().stream().anyMatch(aFrameworkAnnotation());
    }
}
