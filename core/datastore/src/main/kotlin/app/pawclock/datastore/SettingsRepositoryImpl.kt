package app.pawclock.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import app.pawclock.model.CalculationMethod
import app.pawclock.model.ThemeMode
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Реализация [SettingsRepository] поверх DataStore Preferences.
 *
 * Маппинг enum → строка:
 *  - [ThemeMode] хранится как [ThemeMode.id] (stable contract — re-resolved через `fromId`).
 *  - [CalculationMethod] хранится как [Enum.name] (Kotlin-нативное, простое; require'ит
 *    сохранять имена констант стабильными при переименованиях — см. KDoc на CalculationMethod).
 *
 * Восстановление после IOException (повреждённый файл prefs) — fallback на пустые preferences,
 * что даст `AppSettings.Default`. Это рекомендованный паттерн из официальной документации
 * AndroidX DataStore.
 */
@Singleton
class SettingsRepositoryImpl
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) : SettingsRepository {
        override fun observe(): Flow<AppSettings> =
            dataStore.data
                .catch { throwable ->
                    if (throwable is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw throwable
                    }
                }.map(::toAppSettings)

        override suspend fun setThemeMode(themeMode: ThemeMode) {
            dataStore.edit { prefs ->
                prefs[SettingsKeys.THEME_MODE] = themeMode.id
            }
        }

        override suspend fun setLanguageTag(languageTag: String?) {
            dataStore.edit { prefs ->
                if (languageTag == null) {
                    prefs.remove(SettingsKeys.LANGUAGE_TAG)
                } else {
                    prefs[SettingsKeys.LANGUAGE_TAG] = languageTag
                }
            }
        }

        override suspend fun setDynamicColor(enabled: Boolean) {
            dataStore.edit { prefs ->
                prefs[SettingsKeys.DYNAMIC_COLOR] = enabled
            }
        }

        override suspend fun setDefaultCalculationMethod(method: CalculationMethod) {
            dataStore.edit { prefs ->
                prefs[SettingsKeys.DEFAULT_CALCULATION_METHOD] = method.name
            }
        }

        /**
         * Чистая функция маппинга Preferences → AppSettings.
         *
         * Не выносится наружу как public API: тестируется через `observe().first()` —
         * этого достаточно, потому что любая ошибка маппинга проявится в одном из
         * тестов `SettingsRepositoryTest`.
         */
        private fun toAppSettings(prefs: Preferences): AppSettings =
            AppSettings(
                themeMode =
                    prefs[SettingsKeys.THEME_MODE]
                        ?.let(ThemeMode::fromId)
                        ?: ThemeMode.System,
                languageTag = prefs[SettingsKeys.LANGUAGE_TAG],
                dynamicColor = prefs[SettingsKeys.DYNAMIC_COLOR] ?: true,
                defaultCalculationMethod =
                    prefs[SettingsKeys.DEFAULT_CALCULATION_METHOD]
                        ?.let { name -> runCatching { CalculationMethod.valueOf(name) }.getOrNull() }
                        ?: CalculationMethod.EPIGENETIC,
            )
    }
