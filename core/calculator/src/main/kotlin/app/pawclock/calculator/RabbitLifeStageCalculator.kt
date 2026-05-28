// Числовые литералы здесь — табличные пороги стадий жизни кроликов (Oxbow).
// Каждое значение имеет ветеринарный смысл (возраст в годах перехода между стадиями),
// все собраны в KDoc-таблице ниже с указанием первоисточника.
@file:Suppress("detekt:MagicNumber")

package app.pawclock.calculator

import app.pawclock.model.LifeStage
import app.pawclock.model.RabbitSize
import app.pawclock.model.Species

/**
 * Калькулятор стадии жизни кролика по возрасту (Oxbow Rabbit Life Stages, §4.3 спецификации).
 *
 * | Стадия       | Возраст начала          |
 * |--------------|-------------------------|
 * | Infancy      | 0 — 3 мес. (0–0.25)     |
 * | Adolescence  | 3 — 6 мес. (0.25–0.5)   |
 * | YoungAdult   | 6 — 12 мес. (0.5–1.0)   |
 * | Adult        | 1 — 5 лет               |
 * | Senior       | 5+ лет                  |
 *
 * Пороги едины для всех пород ([RabbitSize] влияет только на
 * [expectedLifespanRange], но не на границы стадий).
 *
 * Источники:
 *  - Oxbow Animal Health. *Rabbit Lifespan and Life Stages*.
 *  - House Rabbit Society guidelines.
 *
 * См. также спецификацию PawClock §4.3 и ADR-0006.
 */
data object RabbitLifeStageCalculator : LifeStageCalculator {
    override val species: Species = Species.Rabbit

    /**
     * Унифицированная точка диспатча из [LifeStageCalculator]: делегирует в перегрузку
     * [determine] с явным `size`.
     *
     * @throws IllegalArgumentException если [params] не является [SpeciesParams.Rabbit].
     */
    override fun determine(
        ageInYears: Double,
        params: SpeciesParams,
    ): LifeStage {
        require(params is SpeciesParams.Rabbit) {
            "RabbitLifeStageCalculator requires SpeciesParams.Rabbit, got ${params::class.simpleName}"
        }
        return determine(ageInYears, params.size)
    }

    /**
     * Возвращает стадию жизни кролика по возрасту.
     *
     * @param ageInYears возраст кролика в годах (должен быть > 0).
     * @param size порода/размер — на границы стадий не влияет, принимается для единообразия API.
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     */
    @Suppress("UNUSED_PARAMETER")
    fun determine(
        ageInYears: Double,
        size: RabbitSize,
    ): LifeStage.Rabbit {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        return when {
            ageInYears >= SENIOR_START -> LifeStage.Rabbit.Senior
            ageInYears >= ADULT_START -> LifeStage.Rabbit.Adult
            ageInYears >= YOUNG_ADULT_START -> LifeStage.Rabbit.YoungAdult
            ageInYears >= ADOLESCENCE_START -> LifeStage.Rabbit.Adolescence
            else -> LifeStage.Rabbit.Infancy
        }
    }

    /**
     * Возвращает ожидаемый диапазон продолжительности жизни (в годах) для размера кролика.
     *
     * По House Rabbit Society / AVMA (§4.3): базовый диапазон 8–12 лет, мелкие/карликовые
     * породы живут дольше (до 14), гигантские — короче (6–8).
     *
     * @param size порода/размер кролика.
     */
    fun expectedLifespanRange(size: RabbitSize): ClosedFloatingPointRange<Double> =
        when (size) {
            RabbitSize.Dwarf, RabbitSize.Small -> 8.0..14.0
            RabbitSize.Medium -> 8.0..12.0
            RabbitSize.Large -> 7.0..10.0
            RabbitSize.Giant -> 6.0..8.0
        }

    /** Возраст начала Adolescence (3 месяца). */
    internal const val ADOLESCENCE_START: Double = 0.25

    /** Возраст начала YoungAdult (6 месяцев). */
    internal const val YOUNG_ADULT_START: Double = 0.5

    /** Возраст начала Adult (1 год). */
    internal const val ADULT_START: Double = 1.0

    /** Возраст начала Senior (5 лет). */
    internal const val SENIOR_START: Double = 5.0
}
