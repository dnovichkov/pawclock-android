plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kover)
}

android {
    namespace = "app.pawclock.core.datastore"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // testOptions: JVM unit tests требуют, чтобы Android resources / Robolectric не подтягивались —
    // DataStore Preferences тестируется на временных файлах через PreferenceDataStoreFactory,
    // без Context (см. SettingsRepositoryTest).
    testOptions {
        unitTests.isReturnDefaultValues = true
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

    implementation(project(":core:model"))

    // DataStore Preferences — типизированное key-value хранилище поверх protobuf-файла.
    // Используется для хранения пользовательских настроек (тема, язык, метод расчёта).
    implementation(libs.datastore.preferences)

    // Hilt — DI для DataStoreModule (см. Task 13 в плане).
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // JVM unit tests — используем PreferenceDataStoreFactory.create() с tempDir.
    // PreferenceDataStoreFactory НЕ требует Context, поэтому тесты идут как обычные JVM-юниты.
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
    }
}

// Покрытие :core:datastore не специфицировано явно в §11.4, но репозиторный слой
// заслуживает строгого покрытия — оставляем минимум 80% (как у :core:database).
//
// Из coverage исключаем:
//  - AGP-генерируемый BuildConfig (тривиальный, без runtime);
//  - Hilt-DI модули (DataStoreModule, SettingsRepositoryModule) — требуют Android Context
//    и тестируются интеграционно через `@HiltAndroidTest`, а не юнит-тестами;
//  - hilt_aggregated_deps.* — Hilt-codegen, не наш код;
//  - *_Factory / *_HiltModules — Hilt-сгенерированные классы (KSP output).
//
// Так измеряется покрытие именно репозитория и data-классов — единственный код,
// который имеет смысл покрывать на этом слое в pure JVM.
kover {
    reports {
        filters {
            excludes {
                classes(
                    "*.BuildConfig",
                    "*_Factory",
                    "*_Factory\$*",
                    "*_HiltModules*",
                )
                packages(
                    "app.pawclock.datastore.di",
                    "hilt_aggregated_deps",
                    "dagger.hilt.internal.aggregatedroot.codegen",
                )
            }
        }
        verify {
            rule {
                minBound(80)
            }
        }
    }
}
