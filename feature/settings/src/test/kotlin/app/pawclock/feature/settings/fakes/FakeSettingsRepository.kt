package app.pawclock.feature.settings.fakes

import app.pawclock.datastore.AppSettings
import app.pawclock.datastore.SettingsRepository
import app.pawclock.model.CalculationMethod
import app.pawclock.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory fake [SettingsRepository] для JVM unit-тестов.
 *
 * `observe()` возвращает StateFlow с актуальным snapshot'ом; каждый setter мутирует
 * этот snapshot, что эмулирует поведение DataStore: write → новый snapshot эмитится
 * на подписчиков observe().
 *
 * Тесты могут передавать кастомный [initial] чтобы стартовать с нужным состоянием
 * (например, themeMode=Dark), а также проверять последовательность вызовов через
 * публичные счётчики [writeCount] + [writes].
 */
internal class FakeSettingsRepository(
    initial: AppSettings = AppSettings.Default,
) : SettingsRepository {
    private val internalState = MutableStateFlow(initial)

    /**
     * Журнал всех записей в порядке поступления — для assertions в тестах.
     * Используется чтобы проверить, что fire-and-forget ViewModel действительно дернул setter.
     */
    val writes: MutableList<AppSettings> = mutableListOf()

    val writeCount: Int
        get() = writes.size

    override fun observe(): Flow<AppSettings> = internalState

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        internalState.update { it.copy(themeMode = themeMode) }
        writes.add(internalState.value)
    }

    override suspend fun setLanguageTag(languageTag: String?) {
        internalState.update { it.copy(languageTag = languageTag) }
        writes.add(internalState.value)
    }

    override suspend fun setDynamicColor(enabled: Boolean) {
        internalState.update { it.copy(dynamicColor = enabled) }
        writes.add(internalState.value)
    }

    override suspend fun setDefaultCalculationMethod(method: CalculationMethod) {
        internalState.update { it.copy(defaultCalculationMethod = method) }
        writes.add(internalState.value)
    }
}
