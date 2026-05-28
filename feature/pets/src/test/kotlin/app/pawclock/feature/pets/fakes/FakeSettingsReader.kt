package app.pawclock.feature.pets.fakes

import app.pawclock.domain.settings.SettingsReader
import app.pawclock.model.CalculationMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory fake [SettingsReader] для юнит-тестов feature-ViewModel'ей.
 *
 * Стартовое значение задаётся через [initialMethod] (default — [CalculationMethod.EPIGENETIC]).
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
