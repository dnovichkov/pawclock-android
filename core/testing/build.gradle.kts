plugins {
    alias(libs.plugins.kotlin.jvm)
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
    implementation(libs.kotlin.test)
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.junit.jupiter.api)
    implementation(libs.turbine)
    implementation(libs.mockk)
    implementation(project(":core:model"))

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
