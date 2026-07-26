plugins {
    java
    jacoco
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "eu.dieterbaier"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

val cucumberVersion = "7.18.1"
val archunitVersion = "1.4.1"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:$cucumberVersion")
    testImplementation("org.junit.platform:junit-platform-suite:1.10.3")
    testImplementation("com.tngtech.archunit:archunit-junit5:$archunitVersion")
}

tasks.withType<Test> {
    useJUnitPlatform {
        // The architecture rules are ordinary tests, so they run on every build and
        // gate the pull request through the existing `PR validation` check. Tagging
        // them keeps a deliberate escape hatch for a refactor in progress:
        //   ./gradlew test -PexcludeTags=architecture
        // `toString()` rather than a cast: a Gradle property is Any?, so `as String?`
        // would throw ClassCastException on a non-string value from gradle.properties
        // or the environment.
        val excluded = project.findProperty("excludeTags")?.toString().orEmpty()
            .split(',')
            .map(String::trim)
            .filter(String::isNotEmpty)
        if (excluded.isNotEmpty()) {
            excludeTags(*excluded.toTypedArray())
        }
    }
}

// Convenience only -- the rules already run in `test`, which is what gates the
// pull request. This exists so a maintainer can get the architecture verdict in
// about a second without waiting for Testcontainers to start PostgreSQL.
tasks.register<Test>("architectureTest") {
    description = "Runs only the ArchUnit architecture rules (see ADR-013)."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform {
        includeTags("architecture")
    }
    filter {
        // `CucumberTest` is a JUnit Platform @Suite, and a suite whose filter
        // matches nothing fails with NoTestsDiscoveredException rather than being
        // skipped. Excluding it here keeps the tag the real selector.
        excludeTestsMatching("eu.dieterbaier.budget.CucumberTest")
    }
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    // Activate the local profile (datasource + demo data seeder). Start the
    // database first with `docker compose up -d` / `podman compose up -d`.
    systemProperty("spring.profiles.active", "local")
}

// CON-006: test coverage. The thresholds and the reasoning behind them are
// ADR-018; the exclusions below are listed there too, because an undocumented
// exclusion is how a coverage number stops meaning anything.
//
// Coverage rides `test`, the task the required `PR validation` check already
// runs, for the same reason the ArchUnit rules do (ADR-013): a check that needs
// a separate gate is a check someone forgets to run.
val coverageExclusions = listOf(
    // The boot class belongs to no layer, exactly as in CON-002.
    "eu/dieterbaier/budget/BudgetApplication.class",
    // Local-profile convenience, never deployed and deliberately untested.
    "eu/dieterbaier/budget/dev/**",
)

// Built from the output directory rather than by mapping `classDirectories`,
// whose `.files` resolves at configuration time and silently leaves the
// exclusions unapplied.
fun JacocoReportBase.applyCoverageExclusions() {
    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("classes/java/main")) { exclude(coverageExclusions) },
    )
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named("test"))
    applyCoverageExclusions()
    reports {
        xml.required = true
        csv.required = true
        html.required = true
    }
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.named("test"))
    applyCoverageExclusions()
    violationRules {
        // The global floor. 80 is a convention rather than a derived number --
        // see ADR-018. Its worth is that it cannot silently drop.
        rule {
            element = "BUNDLE"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
        // The money rules carry a higher bar than the average. They are
        // framework-free and trivially testable, and QG-003 is about them, so a
        // gap here must not be payable by trivial coverage elsewhere.
        rule {
            element = "PACKAGE"
            includes = listOf("eu.dieterbaier.budget.domain.*")
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.95".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.90".toBigDecimal()
            }
        }
    }
}

// Verification runs as part of `test`, not after `check`, so a coverage drop
// fails the same command that runs the tests.
tasks.named<Test>("test") {
    finalizedBy(tasks.named("jacocoTestCoverageVerification"))
}
