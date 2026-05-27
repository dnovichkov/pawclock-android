package app.pawclock.domain.pet

import app.pawclock.model.CalculationMethod
import app.pawclock.model.LifeStage

/**
 * Результат расчёта возраста питомца.
 *
 * Возвращается из [CalculatePetAgeUseCase] и используется UI для отображения
 * больших чисел (humanYears) и стадии жизни (lifeStage).
 *
 * @property ageInYears календарный возраст в годах (Double — например, `5.42` для 5 лет 5 месяцев)
 * @property humanYears возраст в человеческих годах по выбранной формуле
 * @property lifeStage стадия жизни на текущий возраст
 * @property method метод расчёта, использованный для получения [humanYears]
 *   (для кошек — всегда [CalculationMethod.EPIGENETIC]: AAFP-формула не имеет SIZE_BASED-варианта,
 *   и поле фиксируется для согласованности UI; для собак — EPIGENETIC или SIZE_BASED).
 */
data class CalculatedAge(
    val ageInYears: Double,
    val humanYears: Double,
    val lifeStage: LifeStage,
    val method: CalculationMethod,
)
