plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "app.pawclock.core.designsystem"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    buildFeatures {
        compose = true
    }

    // Roborazzi screenshot tests требуют Robolectric, который умеет загружать Android
    // ресурсы через AAPT2 только если `includeAndroidResources = true`. См.
    // https://github.com/takahirom/roborazzi#configurations.
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
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
    implementation(project(":core:model"))

    // Compose BOM — единая версионная гарантия для всех compose-* артефактов.
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    // ui-tooling — нужен для @Preview Composable в IDE (компилируется в debug,
    // но не попадает в release благодаря Compose BOM конфигурации).
    debugImplementation(libs.androidx.compose.ui.tooling)

    // JVM unit tests — PawClockThemeTest (без Compose runtime, чистая логика ColorScheme).
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
    // Vintage engine для запуска JUnit 4 Robolectric-тестов рядом с JUnit 5.
    testRuntimeOnly(libs.junit.vintage.engine)

    // Roborazzi screenshot tests — opt-in (см. Task 16, §11 спецификации).
    // Запуск: ./gradlew :core:designsystem:recordRoboImages -Droborazzi.test.record=true
    // По умолчанию пропускаются через Assumptions.assumeTrue.
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)
    testImplementation(libs.robolectric)
    testImplementation(libs.junit4)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.androidx.test.rules)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform {
        // Vintage engine разрешает JUnit 4 (Robolectric) тесты, Jupiter — JUnit 5.
        includeEngines("junit-jupiter", "junit-vintage")
    }
    testLogging {
        events("passed", "failed", "skipped")
    }
}

// Roborazzi записывает PNG-снимки в build/outputs/roborazzi/.
// outputDir не нужен — стандартный путь roborazzi достаточен; явная конфигурация добавится
// при появлении baseline снимков (Plan 2).
