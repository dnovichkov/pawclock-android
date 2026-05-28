plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "app.pawclock"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.pawclock"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // Signing config — Wired only when keystore env-vars присутствуют (release.yml decoder
    // выставляет PAWCLOCK_KEYSTORE_PATH + PAWCLOCK_KEYSTORE_PASSWORD/KEY_ALIAS/KEY_PASSWORD).
    // Без vars (локальный debug build разработчика) — release-сборка не падает на
    // конфигурации, она просто остаётся неподписанной и signingConfig не привязан.
    // Это покрывает: (a) CI release.yml → подписанный AAB для Play; (b) локальные
    // assembleRelease без секретов → unsigned APK для smoke-проверки.
    val releaseKeystorePath: String? = System.getenv("PAWCLOCK_KEYSTORE_PATH")
    val releaseKeystorePassword: String? = System.getenv("PAWCLOCK_KEYSTORE_PASSWORD")
    val releaseKeyAlias: String? = System.getenv("PAWCLOCK_KEY_ALIAS")
    val releaseKeyPassword: String? = System.getenv("PAWCLOCK_KEY_PASSWORD")
    val hasReleaseSigning =
        !releaseKeystorePath.isNullOrBlank() &&
            !releaseKeystorePassword.isNullOrBlank() &&
            !releaseKeyAlias.isNullOrBlank() &&
            !releaseKeyPassword.isNullOrBlank()

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseKeystorePath!!)
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            // R8/minify остаётся выключенным в Plan 1 MVP: вся библиотечная цепочка
            // (Hilt, Room, kotlinx-serialization, Compose) поставляет consumer-rules,
            // но без явной верификации на устройстве с включённым минификатором
            // включать его в release нельзя — это отдельная задача в Plan 2.
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
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
    // appcompat нужен для AppCompatDelegate.setApplicationLocales — in-app language picker
    // (Task 22). Используется только адаптером LocaleHelper в :app/locale/, не feature-модулями.
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(project(":core:model"))
    implementation(project(":core:calculator"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":feature:pets"))
    implementation(project(":feature:editor"))
    implementation(project(":feature:quickcalc"))
    implementation(project(":feature:settings"))

    // Compose BOM — единая версионная гарантия для всех compose-* артефактов.
    // Re-enabled в Task 17 одновременно с kotlin.compose plugin и MainActivity.
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Activity + ViewModel + Navigation Compose интеграция.
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.androidx.compose.hilt.navigation)

    // Hilt — DI runtime + KSP-генератор. Совместно с @HiltAndroidApp на PawClockApplication.
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlinx.coroutines.test)
    testRuntimeOnly(libs.junit.jupiter.engine)

    // Smoke androidTest — MainActivity launch без crash. Использует только
    // androidx.test.core (ActivityScenario) + ext-junit, без compose-ui-test-junit4
    // (metadata 1.7.5 отсутствует в offline cache на момент Task 17 — выбираем
    // более узкий smoke-тест: запускается Activity, не падает на init).
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.core)

    // Compose UI tests (Task 23) — createAndroidComposeRule<MainActivity>() для
    // MainNavigationTest + AppLaunchTest. Используются compose-ui-test-junit4 1.11.1
    // (доступно через BOM 2026.05.00, см. Task 17 bump notes). Запуск на эмуляторе
    // в nightly.yml workflow (reactivecircus/android-emulator-runner матрица API 24/30/35).
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
