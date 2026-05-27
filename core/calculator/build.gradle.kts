plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kover)
}

java {
    toolchain {
        languageVersion.set(
            JavaLanguageVersion.of(
                libs.versions.javaToolchain
                    .get()
                    .toInt(),
            ),
        )
    }
}

kotlin {
    jvmToolchain(
        libs.versions.javaToolchain
            .get()
            .toInt(),
    )
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(project(":core:model"))

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)

    // Property-based testing (Task 11). Используется через @Test + runBlocking { checkAll(...) }
    // — без kotest-runner-junit5, чтобы сохранить единый JUnit5-стиль тестов в модуле.
    testImplementation(libs.kotest.property)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlinx.coroutines.core)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
    }
}

// Покрытие :core:calculator должно быть ≥ 95% по §11.4 спецификации.
// См. также Task 11 в плане pawclock-foundation-and-dog-cat-mvp.md.
kover {
    reports {
        verify {
            rule {
                minBound(95)
            }
        }
    }
}
