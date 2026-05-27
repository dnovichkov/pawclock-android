plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kover)
}

android {
    namespace = "app.pawclock.core.database"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// Room schema export — позволяет генерировать JSON-схему БД для миграционных тестов
// (см. §3.5 спецификации + Task 12 в плане). Schema-файлы будут в
// core/database/schemas/ и должны коммититься (для diff'ов миграций).
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
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

    // Room — type-safe SQLite ORM. KSP-генератор требует room-compiler.
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Hilt — DI-инъекция для DatabaseModule (см. Task 12 в плане).
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // JVM unit tests (PetMapperTest) — без Room runtime, чистый маппинг.
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)

    // Android instrumented tests (PetDaoTest, MigrationTest) — in-memory Room.
    // ВАЖНО: используются только JUnit 4 (org.junit.Assert) + kotlinx-coroutines-test;
    // не добавляйте kotlin-test сюда — она тянет kotlin-test-junit, которого нет в offline-кеше.
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
    }
}

// Покрытие :core:database должно быть ≥ 80% по §11.4 спецификации.
// Verify-rule срабатывает на ./gradlew :core:database:koverVerify.
kover {
    reports {
        verify {
            rule {
                minBound(80)
            }
        }
    }
}
