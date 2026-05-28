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
    namespace = "app.pawclock.feature.pets"
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
    // Material icons — Icons.Filled.Add, Icons.AutoMirrored.Filled.ArrowBack, Icons.Filled.Edit.
    // Core (не extended) — экономия APK: ~200KB vs. ~6MB extended.
    implementation(libs.androidx.compose.material.icons.core)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Lifecycle + ViewModel Compose integration — для hiltViewModel() и collectAsStateWithLifecycle.
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.androidx.compose.hilt.navigation)

    // Hilt — DI для @HiltViewModel'ей.
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // JVM unit tests — ViewModel'и тестируются как чистые StateFlow-machine'ы
    // с Turbine для проверки последовательных эмиссий + fakes из :core:domain (testFixtures-style).
    // :core:calculator нужен в test classpath, чтобы тесты могли инстанцировать реальные
    // калькуляторы внутри CalculatePetAgeUseCase (faking-уровень: domain UseCase + real
    // calculator pure-Kotlin) без необходимости создавать дополнительные test-dub'ы.
    testImplementation(project(":core:calculator"))
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testRuntimeOnly(libs.junit.jupiter.engine)

    // Android instrumented tests — Compose UI тесты PetsListScreen/PetDetailScreen
    // через createAndroidComposeRule. Compose-ui-test-junit4 1.11.1 в offline cache
    // (управляется BOM 2026.05.00). Запуск — на эмуляторе в nightly.yml.
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

// Покрытие feature-ViewModel'ей должно быть ≥ 80% по §11.4 спецификации.
// Verify-rule срабатывает на ./gradlew :feature:pets:koverVerify.
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
                // ViewModel'и (business logic) — главная цель coverage.
                packages(
                    "app.pawclock.feature.pets.list.ui",
                    "app.pawclock.feature.pets.detail.ui",
                    // Hilt-generated DI пакеты (Factory/HiltModules) — pure DI wiring,
                    // не имеют логики, тесты на DI делает Hilt-runtime в androidTest.
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
                    // LocaleProvider.current() в production читает Locale.getDefault().language —
                    // тестируется через FakeLocaleProvider; реальный код — single-line wrapper,
                    // покрываемый только androidTest'ами / integration-тестом.
                    "*LocaleProvider",
                )
            }
        }
    }
}
