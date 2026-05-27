package app.pawclock.domain.settings

import app.pawclock.model.CalculationMethod
import kotlinx.coroutines.flow.Flow

/**
 * Узкий port (по терминологии Hexagonal Architecture) для чтения настроек из домена.
 *
 * UseCase'ам не нужен весь `AppSettings` snapshot — только дефолтный метод расчёта
 * для собак (см. [CalculatePetAgeUseCase]). Поэтому объявлен максимально узкий контракт:
 * домен не зависит от `:core:datastore` напрямую (Android-library кросс-зависимость),
 * а адаптер `SettingsReaderAdapter` в `:core:datastore`/`:app` делегирует в
 * `SettingsRepository.observe().map { it.defaultCalculationMethod }`.
 *
 * Тестируется через `FakeSettingsReader(initial = EPIGENETIC)`.
 */
interface SettingsReader {
    /**
     * Реактивный поток текущего дефолтного метода расчёта.
     *
     * Эмитит новое значение на каждое изменение настройки в DataStore.
     * Если значение никогда не было записано, первая эмиссия — дефолт
     * ([CalculationMethod.EPIGENETIC] по ADR-0006).
     */
    fun observeDefaultCalculationMethod(): Flow<CalculationMethod>
}
