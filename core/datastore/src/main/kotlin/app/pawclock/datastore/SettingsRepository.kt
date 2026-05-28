package app.pawclock.datastore

import app.pawclock.model.CalculationMethod
import app.pawclock.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий пользовательских настроек приложения PawClock.
 *
 * Дизайн-решения:
 *  - [observe] возвращает Flow<AppSettings>, а не отдельные Flow<ThemeMode>, Flow<String?>, и т.д.
 *    Причина: ViewModel'и обычно `combine(...)` или `map` к нужному полю, и одна
 *    Flow<AppSettings> устраняет необходимость пересобирать combine при добавлении новой настройки.
 *  - Сеттеры — `suspend`, потому что DataStore.edit { } сам suspend.
 *    Реализация не имеет thread-блокирующих синхронных версий — это намеренно.
 *  - Никаких initial-load методов: первая эмиссия Flow вернёт `AppSettings.Default` если
 *    файл DataStore ещё пустой.
 *
 * Тестируется через `SettingsRepositoryImpl(testDataStore)` без Hilt-инъекций
 * (см. `SettingsRepositoryTest.kt`).
 */
interface SettingsRepository {
    /**
     * Реактивный поток текущих настроек.
     *
     * Эмитит новый [AppSettings] на каждое изменение в DataStore.
     * При повреждённых/неизвестных значениях (например, после удаления enum-константы
     * в новой версии приложения) — fallback на [AppSettings.Default] для затронутых полей.
     */
    fun observe(): Flow<AppSettings>

    /** Устанавливает режим темы. */
    suspend fun setThemeMode(themeMode: ThemeMode)

    /**
     * Устанавливает язык приложения по BCP 47 tag, либо `null` для возврата к системной локали.
     * Применяется через `AppCompatDelegate.setApplicationLocales` в `:app` (Task 22).
     */
    suspend fun setLanguageTag(languageTag: String?)

    /** Включает/выключает Material You dynamic color (Android 12+). */
    suspend fun setDynamicColor(enabled: Boolean)

    /** Устанавливает дефолтный метод расчёта возраста собак (Wang vs Size). */
    suspend fun setDefaultCalculationMethod(method: CalculationMethod)
}
