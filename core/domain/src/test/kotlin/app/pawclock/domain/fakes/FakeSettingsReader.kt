package app.pawclock.domain.fakes

import app.pawclock.domain.settings.SettingsReader
import app.pawclock.model.CalculationMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory fake [SettingsReader] для юнит-тестов.
 *
 * Стартовое значение задаётся через [initialMethod] (default — [CalculationMethod.EPIGENETIC]).
 * Тесты могут менять значение через [setMethod] для проверки реакции UseCase на смену
 * пользовательских настроек.
 */
class FakeSettingsReader(
    initialMethod: CalculationMethod = CalculationMethod.EPIGENETIC,
) : SettingsReader {
    private val flow = MutableStateFlow(initialMethod)

    override fun observeDefaultCalculationMethod(): Flow<CalculationMethod> = flow.asStateFlow()

    fun setMethod(method: CalculationMethod) {
        flow.value = method
    }
}
