package app.pawclock.datastore

import app.pawclock.model.CalculationMethod
import app.pawclock.model.ThemeMode

/**
 * Иммутабельный snapshot пользовательских настроек приложения PawClock.
 *
 * Снимается из DataStore через [SettingsRepository.observe] как Flow<AppSettings>:
 * новый instance создаётся на каждое изменение, никаких реактивных полей внутри
 * самого data class — пусть UI/ViewModel вытаскивает только нужное поле через `map`.
 *
 * @property themeMode Светлая/тёмная/системная тема. Default: [ThemeMode.System].
 * @property languageTag BCP 47 language tag (`"ru"`, `"en"`) или `null` для следования
 *   системной локали. Применяется через `AppCompatDelegate.setApplicationLocales`
 *   (см. Task 22 в плане).
 * @property dynamicColor Активировать Material You dynamic color на Android 12+ (API 31+).
 *   На устройствах с API < 31 setting игнорируется, fallback на статическую палитру.
 *   Default: `true` (opt-in).
 * @property defaultCalculationMethod Метод расчёта возраста, выбранный по умолчанию
 *   в Quick Calculator и при создании питомца. Для собак влияет на выбор Wang vs Size.
 *   Default: [CalculationMethod.EPIGENETIC] (см. ADR-0006).
 */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val languageTag: String? = null,
    val dynamicColor: Boolean = true,
    val defaultCalculationMethod: CalculationMethod = CalculationMethod.EPIGENETIC,
) {
    companion object {
        /**
         * Дефолтный snapshot — используется как fallback, если файл DataStore
         * ещё не создан, либо при corrupted-данных.
         */
        val Default: AppSettings = AppSettings()
    }
}
