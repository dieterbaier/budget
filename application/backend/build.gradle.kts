plugins {
    java
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
