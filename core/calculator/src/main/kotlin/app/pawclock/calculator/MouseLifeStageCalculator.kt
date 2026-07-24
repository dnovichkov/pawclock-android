// Числовые литералы здесь — табличные пороги стадий жизни мышей (Dutta & Sengupta 2016 + §4.6).
// Каждое значение имеет ветеринарный смысл (возраст в годах перехода между стадиями),
// все собраны в KDoc-таблице ниже с указанием первоисточника.
@file:Suppress("detekt:MagicNumber")

package app.pawclock.calculator

import app.pawclock.model.LifeStage
import app.pawclock.model.Species

/**
 * Калькулятор стадии жизни мыши по возрасту (Dutta & Sengupta 2016, §4.6 спецификации).
 *
 * | Стадия    | Возраст начала                          |
 * |-----------|-----------------------------------------|
 * | Pup       | 0 — ~3 нед. (0–0.058, до отъёма)        |
 * | Juvenile  | ~3 нед. — 3 мес. (0.058–0.25)           |
 * | Adult     | 3 мес. — 12 мес. (0.25–1.0)             |
 * | Senior    | 12 мес. — 24 мес. («senior с 12 мес.»)  |
 * | EndOfLife | 24+ мес. (приближение к ЧЖ 1–3 года)    |
 *
 * Пороги едины — у мыши нет подкатегорий.
 *
 * Источник:
 *  - Dutta, S., & Sengupta, P. (2016). *Men and mice: Relating their ages.*
 *    Life Sciences, 152, 244–248. DOI: 10.1016/j.lfs.2015.10.025.
 *
 * См. также спецификацию PawClock §4.6 и ADR-0006.
 */
data object MouseLifeStageCalculator : LifeStageCalculator {
    override val species: Species = Species.Mouse

    /**
     * Унифицированная точка диспатча из [LifeStageCalculator]: делегирует в перегрузку без подкатегории.
     *
     * @throws IllegalArgumentException если [params] не является [SpeciesParams.Mouse].
     */
    override fun determine(
        ageInYears: Double,
        params: SpeciesParams,
    ): LifeStage {
        require(params is SpeciesParams.Mouse) {
            "MouseLifeStageCalculator requires SpeciesParams.Mouse, got ${params::class.simpleName}"
        }
        return determine(ageInYears)
    }

    /**
     * Возвращает стадию жизни мыши по возрасту.
     *
     * @param ageInYears возраст мыши в годах (должен быть > 0).
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     */
    fun determine(ageInYears: Double): LifeStage.Mouse {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        return when {
            ageInYears >= END_OF_LIFE_START -> LifeStage.Mouse.EndOfLife
            ageInYears >= SENIOR_START -> LifeStage.Mouse.Senior
            ageInYears >= ADULT_START -> LifeStage.Mouse.Adult
            ageInYears >= JUVENILE_START -> LifeStage.Mouse.Juvenile
            else -> LifeStage.Mouse.Pup
        }
    }

    /**
     * Возвращает ожидаемый диапазон продолжительности жизни мыши (в годах).
     *
     * По Dutta & Sengupta 2016 / §4.6: ЧЖ 1–3 года (подкатегорий нет).
     */
    fun expectedLifespanRange(): ClosedFloatingPointRange<Double> = 1.0..3.0

    /** Возраст начала Juvenile (~3 недели, отъём). */
    internal const val JUVENILE_START: Double = 0.058

    /** Возраст начала Adult (3 месяца, социальная зрелость). */
    internal const val ADULT_START: Double = 0.25

    /** Возраст начала Senior (12 месяцев, §4.6). */
    internal const val SENIOR_START: Double = 1.0

    /** Возраст начала EndOfLife (24 месяца). */
    internal const val END_OF_LIFE_START: Double = 2.0
}
