plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
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
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    implementation(project(":core:model"))
    implementation(project(":core:calculator"))

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.turbine)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
    }
}

// :core:domain покрытие должно быть ≥ 90% по §11.4 спецификации (целевые показатели tests).
// Verify-rule срабатывает на ./gradlew :core:domain:koverVerify.
kover {
    reports {
        verify {
            rule {
                minBound(90)
            }
        }
    }
}
