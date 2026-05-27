package app.pawclock.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Стабильные ключи для DataStore Preferences.
 *
 * Имена ключей — это часть persistent contract'а: их **нельзя** переименовывать
 * без миграции (`DataMigration<Preferences>`), иначе пользовательские настройки
 * пропадут после обновления. Все ключи snake_case по conventiom DataStore.
 *
 * `internal`, потому что доступ к ним нужен только из этого модуля
 * (production-код `SettingsRepositoryImpl` + тесты, имитирующие повреждённые данные).
 */
internal object SettingsKeys {
    /** Theme mode: stored as [app.pawclock.model.ThemeMode.id] (например `"dark"`). */
    val THEME_MODE = stringPreferencesKey("theme_mode")

    /** BCP 47 language tag (`"ru"`, `"en"`) или отсутствует (= follow system). */
    val LANGUAGE_TAG = stringPreferencesKey("language_tag")

    /** Включён ли Material You dynamic color (Android 12+). */
    val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")

    /** Default calculation method: stored as [Enum.name] (например `"EPIGENETIC"`). */
    val DEFAULT_CALCULATION_METHOD = stringPreferencesKey("default_calculation_method")
}
