package app.pawclock.designsystem

/**
 * Константы для Roborazzi screenshot-тестов в `:core:designsystem`.
 *
 * SCREENSHOT_TEST_SDK — Android API level используемый Robolectric. Должен соответствовать
 * `android-all-instrumented` jar, доступному в локальном `~/.m2/repository/` или загружаемому
 * Robolectric'ом из maven central. API 30 (Android 11) — single точка для всех тестов
 * модуля; bump при необходимости.
 *
 * SCREENSHOT_DIR — относительный путь от рабочей директории модуля (`core/designsystem/`)
 * до выходной папки baseline-снимков. Roborazzi сохранит здесь PNG.
 */
internal const val SCREENSHOT_TEST_SDK = 30
internal const val SCREENSHOT_DIR = "src/test/screenshots"
