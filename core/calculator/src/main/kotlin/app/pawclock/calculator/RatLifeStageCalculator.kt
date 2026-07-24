// Числовые литералы здесь — табличные пороги стадий жизни крыс (Sengupta 2013 + §4.6).
// Каждое значение имеет ветеринарный смысл (возраст в годах перехода между стадиями),
// все собраны в KDoc-таблице ниже с указанием первоисточника.
@file:Suppress("detekt:MagicNumber")

package app.pawclock.calculator

import app.pawclock.model.LifeStage
import app.pawclock.model.Species

/**
 * Калькулятор стадии жизни крысы по возрасту (Sengupta 2013, §4.6 спецификации).
 *
 * | Стадия    | Возраст начала                          |
 * |-----------|-----------------------------------------|
 * | Pup       | 0 — ~3 нед. (0–0.058, до отъёма)        |
 * | Juvenile  | ~3 нед. — 3 мес. (0.058–0.25)           |
 * | Adult     | 3 мес. — 18 мес. (0.25–1.5)             |
 * | Senior    | 18 мес. — 30 мес. («senior с 18 мес.»)  |
 * | EndOfLife | 30+ мес. (приближение к ЧЖ 2–3 года)    |
 *
 * Пороги едины — у крысы нет подкатегорий.
 *
 * Источник:
 *  - Sengupta, P. (2013). *The Laboratory Rat: Relating Its Age With Human's.*
 *    International Journal of Preventive Medicine, 4(6), 624–630. PMC3733029.
 *
 * См. также спецификацию PawClock §4.6 и ADR-0006.
 */
data object RatLifeStageCalculator : LifeStageCalculator {
    override val species: Species = Species.Rat

    /**
     * Унифицированная точка диспатча из [LifeStageCalculator]: делегирует в перегрузку без подкатегории.
     *
     * @throws IllegalArgumentException если [params] не является [SpeciesParams.Rat].
     */
    override fun determine(
        ageInYears: Double,
        params: SpeciesParams,
    ): LifeStage {
        require(params is SpeciesParams.Rat) {
            "RatLifeStageCalculator requires SpeciesParams.Rat, got ${params::class.simpleName}"
        }
        return determine(ageInYears)
    }

    /**
     * Возвращает стадию жизни крысы по возрасту.
     *
     * @param ageInYears возраст крысы в годах (должен быть > 0).
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     */
    fun determine(ageInYears: Double): LifeStage.Rat {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        return when {
            ageInYears >= END_OF_LIFE_START -> LifeStage.Rat.EndOfLife
            ageInYears >= SENIOR_START -> LifeStage.Rat.Senior
            ageInYears >= ADULT_START -> LifeStage.Rat.Adult
            ageInYears >= JUVENILE_START -> LifeStage.Rat.Juvenile
            else -> LifeStage.Rat.Pup
        }
    }

    /**
     * Возвращает ожидаемый диапазон продолжительности жизни крысы (в годах).
     *
     * По Sengupta 2013 / §4.6: ЧЖ 2–3 года (подкатегорий нет).
     */
    fun expectedLifespanRange(): ClosedFloatingPointRange<Double> = 2.0..3.0

    /** Возраст начала Juvenile (~3 недели, отъём). */
    internal const val JUVENILE_START: Double = 0.058

    /** Возраст начала Adult (3 месяца, социальная зрелость). */
    internal const val ADULT_START: Double = 0.25

    /** Возраст начала Senior (18 месяцев, §4.6). */
    internal const val SENIOR_START: Double = 1.5

    /** Возраст начала EndOfLife (30 месяцев). */
    internal const val END_OF_LIFE_START: Double = 2.5
}
