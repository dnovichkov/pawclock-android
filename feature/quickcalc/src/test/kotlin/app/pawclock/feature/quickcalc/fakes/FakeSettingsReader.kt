package app.pawclock.feature.quickcalc.fakes

import app.pawclock.domain.settings.SettingsReader
import app.pawclock.model.CalculationMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory fake [SettingsReader] для юнит-тестов [QuickCalcViewModel].
 *
 * Аналог [app.pawclock.feature.pets.fakes.FakeSettingsReader] — копируем здесь,
 * пока `:core:testing` не оформлен как shared-fixtures модуль (отложено на Plan 2).
 *
 * Стартовое значение задаётся через [initialMethod] (default — [CalculationMethod.EPIGENETIC]).
 * Quick Calculator реально читает settings через [CalculatePetAgeUseCase] только когда
 * [QuickCalcEvent.SetMethod] не вызывался — поскольку ViewModel передаёт явный
 * methodOverride на каждом Calculate, эта зависимость в тестах нейтральна.
 */
class FakeSettingsReader(
    initialMethod: CalculationMethod = CalculationMethod.EPIGENETIC,
) : SettingsReader {
    private val flow = MutableStateFlow(initialMethod)

    override fun observeDefaultCalculationMethod(): Flow<CalculationMethod> = flow.asStateFlow()
}
