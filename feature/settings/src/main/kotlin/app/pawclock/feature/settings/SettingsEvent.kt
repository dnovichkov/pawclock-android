package app.pawclock.feature.settings

import app.pawclock.domain.export.ExportFormat
import app.pawclock.domain.import_.ImportStrategy
import app.pawclock.model.CalculationMethod
import app.pawclock.model.ThemeMode

/**
 * События UI экрана настроек (Task 21 / Plan 1, MVI).
 *
 * Каждое событие — иммутабельное намерение пользователя; [SettingsViewModel.handleEvent]
 * мапит его в вызов соответствующего `suspend setter` в `SettingsRepository` (для настроек)
 * либо в export/import-операцию (§3.5, Plan 2 Task 21). ViewModel не держит явный state —
 * он целиком derived из `repository.observe()`, поэтому события можно считать "командами"
 * в command-query split.
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

    /**
     * Пользователь выбрал формат и инициировал экспорт. ViewModel запоминает [format] и
     * эмитит [SettingsEffect.RequestSaveLocation], чтобы UI открыл SAF `ACTION_CREATE_DOCUMENT`.
     */
    data class ExportRequested(
        val format: ExportFormat,
    ) : SettingsEvent

    /**
     * Пользователь выбрал файл назначения через SAF. ViewModel сериализует питомцев в ранее
     * выбранном формате, пишет в [uriString] и эмитит [SettingsEffect.ExportComplete].
     */
    data class ExportLocationSelected(
        val uriString: String,
    ) : SettingsEvent

    /**
     * Пользователь инициировал импорт. ViewModel эмитит [SettingsEffect.RequestOpenLocation],
     * чтобы UI открыл SAF `ACTION_OPEN_DOCUMENT`. Стратегия слияния передаётся позже — с
     * выбранным файлом (см. [ImportLocationSelected]).
     */
    data object ImportRequested : SettingsEvent

    /**
     * Пользователь выбрал файл-источник через SAF. ViewModel читает содержимое, импортирует
     * по [strategy] и эмитит [SettingsEffect.ImportComplete] либо [SettingsEffect.ImportError].
     */
    data class ImportLocationSelected(
        val uriString: String,
        val strategy: ImportStrategy,
    ) : SettingsEvent
}
