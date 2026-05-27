import io.gitlab.arturbosch.detekt.Detekt
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
}

// Apply static-analysis plugins to every subproject. ktlint-gradle and detekt
// gracefully no-op on modules without Kotlin sources, so blanket apply is safe.
// Plugin markers + impl jars are pre-cached offline (Task 1 / ADR-0001).
subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    extensions.configure<KtlintExtension> {
        // Pinned ktlint-CLI version (matches what is cached at
        // ~/.gradle/caches/.../com.pinterest.ktlint/ktlint-cli/1.4.0). Bumping this
        // requires a fresh download, so keep aligned with the offline cache until
        // network is reliably available in CI.
        version.set("1.4.0")
        android.set(true)
        ignoreFailures.set(false)
        filter {
            exclude { entry -> entry.file.absolutePath.contains("${File.separator}build${File.separator}") }
        }
        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        }
    }

    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        buildUponDefaultConfig = true
        allRules = false
        config.setFrom(files("${rootDir}/detekt.yml"))
        baseline = file("${projectDir}/detekt-baseline.xml")
        ignoreFailures = false
        parallel = true
    }

    tasks.withType<Detekt>().configureEach {
        // detekt 1.23 supports "1.8" .. "21"; matches Java 17 toolchain set per-module.
        jvmTarget = "17"
        reports {
            html.required.set(true)
            xml.required.set(true)
            txt.required.set(false)
            sarif.required.set(false)
            md.required.set(false)
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
