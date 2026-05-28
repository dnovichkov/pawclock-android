plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kover)
}

android {
    namespace = "app.pawclock.feature.quickcalc"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))

    // Compose BOM — единая версионная гарантия для всех compose-* артефактов.
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    // Material icons — Icons.AutoMirrored.Filled.ArrowBack для toolbar.
    implementation(libs.androidx.compose.material.icons.core)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Lifecycle + ViewModel Compose integration — для hiltViewModel() и collectAsStateWithLifecycle.
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.androidx.compose.hilt.navigation)

    // Hilt — DI для @HiltViewModel'и.
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // JVM unit tests — ViewModel тестируется как pure StateFlow-machine с реальным
    // CalculatePetAgeUseCase поверх :core:calculator (нет нужды в калькулятор-фейках).
    testImplementation(project(":core:calculator"))
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testRuntimeOnly(libs.junit.jupiter.engine)

    // Android instrumented tests — Compose UI тесты QuickCalcScreen
    // через createComposeRule. Запуск — на эмуляторе в nightly.yml.
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
    }
}

// Покрытие feature-ViewModel'и должно быть ≥ 80% по §11.4 спецификации.
// Verify-rule срабатывает на ./gradlew :feature:quickcalc:koverVerify.
kover {
    reports {
        verify {
            rule {
                minBound(80)
            }
        }
        filters {
            excludes {
                // Compose UI композаблы — не покрываются JVM unit-тестами (только androidTest).
                // ViewModel (business logic) — главная цель coverage.
                packages(
                    "app.pawclock.feature.quickcalc.ui",
                    // Hilt-generated DI пакеты (Factory/HiltModules) — pure DI wiring.
                    "hilt_aggregated_deps",
                    "dagger.hilt.internal.aggregatedroot.codegen",
                )
                classes(
                    "*ComposableSingletons*",
                    "*\$ComposableSingletons*",
                    // Hilt KSP-генерируемые классы — не имеют ручной логики.
                    "*_Factory*",
                    "*_HiltModules*",
                    "*_HiltModules_*",
                    "*_GeneratedInjector*",
                    "*_MembersInjector*",
                )
            }
        }
    }
}
