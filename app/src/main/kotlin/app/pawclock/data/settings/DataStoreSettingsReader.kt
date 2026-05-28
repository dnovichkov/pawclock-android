package app.pawclock.data.settings

import app.pawclock.datastore.SettingsRepository
import app.pawclock.domain.settings.SettingsReader
import app.pawclock.model.CalculationMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Адаптер [SettingsReader] поверх [SettingsRepository] (паттерн Adapter / Port-and-Adapter).
 *
 * Изолирует `:core:domain` (pure-Kotlin JVM) от прямой зависимости на `:core:datastore`
 * (Android library с DataStore). Адаптер живёт в `:app`, где обе зависимости уже на classpath,
 * и делегирует чтение узкого поля (`AppSettings.defaultCalculationMethod`) в общий поток
 * настроек.
 *
 * Производительность: `map { it.defaultCalculationMethod }` дёшев, потому что
 * `SettingsRepository.observe()` возвращает Flow<AppSettings>, а `map` — холодная
 * операция без подписки до момента сбора.
 *
 * В Task 17, после переезда `:app` на Hilt, этот класс получит `@Inject constructor`
 * и `@Binds` в DomainModule. До этого — простая инстанциация в [app.pawclock.data.domain.di.DomainModule].
 */
class DataStoreSettingsReader(
    private val settingsRepository: SettingsRepository,
) : SettingsReader {
    override fun observeDefaultCalculationMethod(): Flow<CalculationMethod> =
        settingsRepository.observe().map { it.defaultCalculationMethod }
}
