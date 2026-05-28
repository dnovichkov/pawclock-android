package app.pawclock.feature.settings

import app.pawclock.model.CalculationMethod
import app.pawclock.model.ThemeMode

/**
 * События UI экрана настроек (Task 21 / Plan 1, MVI).
 *
 * Каждое событие — иммутабельное намерение пользователя; [SettingsViewModel.handleEvent]
 * мапит его в вызов соответствующего `suspend setter` в `SettingsRepository`. ViewModel
 * не держит явный state — он целиком derived из `repository.observe()`, поэтому события
 * можно считать "командами" в command-query split.
 */
sealed interface SettingsEvent {
    /** Сменить режим темы (Light / Dark / System). */
    data class SetThemeMode(
        val themeMode: ThemeMode,
    ) : SettingsEvent

    /**
     * Сменить язык приложения по BCP 47 tag, либо `null` — следовать системной локали.
     * UI преобразует выбор пользователя в `null` / `"ru"` / `"en"`.
     */
    data class SetLanguageTag(
        val languageTag: String?,
    ) : SettingsEvent

    /** Вкл/выкл Material You dynamic color. */
    data class SetDynamicColor(
        val enabled: Boolean,
    ) : SettingsEvent

    /** Сменить дефолтный метод расчёта возраста для собак (Wang vs Size). */
    data class SetDefaultCalculationMethod(
        val method: CalculationMethod,
    ) : SettingsEvent
}
