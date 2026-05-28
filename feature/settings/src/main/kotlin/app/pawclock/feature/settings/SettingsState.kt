package app.pawclock.feature.settings

import app.pawclock.model.CalculationMethod
import app.pawclock.model.ThemeMode

/**
 * UI-снапшот настроек приложения для экрана Settings (Task 21 / Plan 1).
 *
 * Иммутабельный snapshot, который ViewModel получает из `SettingsRepository.observe()`
 * и публикует через `StateFlow<SettingsState>`. Каждое поле — 1-к-1 с
 * [app.pawclock.datastore.AppSettings]; отдельный UI-класс используется чтобы:
 *  - изолировать feature-слой от datastore-типов (Hexagonal port),
 *  - оставить место для UI-only полей в будущем (например, `isLoading`, `pendingRestart`)
 *    без модификации доменного `AppSettings`.
 *
 * @property themeMode Light / Dark / System.
 * @property languageTag BCP 47 tag (`"ru"`, `"en"`) или `null` для системной локали.
 * @property dynamicColor Material You dynamic color (Android 12+, игнорируется на API < 31).
 * @property defaultCalculationMethod Метод по умолчанию для расчёта возраста собак (Wang vs Size).
 */
data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.System,
    val languageTag: String? = null,
    val dynamicColor: Boolean = true,
    val defaultCalculationMethod: CalculationMethod = CalculationMethod.EPIGENETIC,
) {
    companion object {
        /** Дефолтное состояние — используется как initialValue для StateFlow до первой эмиссии. */
        val Default: SettingsState = SettingsState()
    }
}
