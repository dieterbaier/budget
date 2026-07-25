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
        val excluded = (findProperty("excludeTags") as String?)
            .orEmpty()
            .split(',')
            .map(String::trim)
            .filter(String::isNotEmpty)
        if (excluded.isNotEmpty()) {
            excludeTags(*excluded.toTypedArray())
        }
    }
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    // Activate the local profile (datasource + demo data seeder). Start the
    // database first with `docker compose up -d` / `podman compose up -d`.
    systemProperty("spring.profiles.active", "local")
}
