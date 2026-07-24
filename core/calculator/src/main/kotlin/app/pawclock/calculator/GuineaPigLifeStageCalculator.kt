// Числовые литералы здесь — табличные пороги стадий жизни морских свинок (Oxbow).
// Каждое значение имеет ветеринарный смысл (возраст в годах перехода между стадиями),
// все собраны в KDoc-таблице ниже с указанием первоисточника.
@file:Suppress("detekt:MagicNumber")

package app.pawclock.calculator

import app.pawclock.model.LifeStage
import app.pawclock.model.Species

/**
 * Калькулятор стадии жизни морской свинки по возрасту (Oxbow + Animallama, §4.5 спецификации).
 *
 * | Стадия     | Возраст начала                       |
 * |------------|--------------------------------------|
 * | Pup        | 0 — ~3 нед. (0–0.058, до отъёма)     |
 * | Juvenile   | ~3 нед. — 5 мес. (0.058–0.42)        |
 * | Adult      | 5 мес. — 4 года (0.42–4.0)           |
 * | Senior     | 4 — 6 лет («senior с 4 лет», §4.5)   |
 * | Geriatric  | 6+ лет                               |
 *
 * Пороги едины — у морской свинки нет подкатегорий.
 *
 * Источники:
 *  - Oxbow Animal Health. *Guinea Pig Lifespan and Life Stages*.
 *  - Animallama. *Guinea pig life stages*.
 *
 * См. также спецификацию PawClock §4.5 и ADR-0006.
 */
data object GuineaPigLifeStageCalculator : LifeStageCalculator {
    override val species: Species = Species.GuineaPig

    /**
     * Унифицированная точка диспатча из [LifeStageCalculator]: делегирует в перегрузку без подкатегории.
     *
     * @throws IllegalArgumentException если [params] не является [SpeciesParams.GuineaPig].
     */
    override fun determine(
        ageInYears: Double,
        params: SpeciesParams,
    ): LifeStage {
        require(params is SpeciesParams.GuineaPig) {
            "GuineaPigLifeStageCalculator requires SpeciesParams.GuineaPig, got ${params::class.simpleName}"
        }
        return determine(ageInYears)
    }

    /**
     * Возвращает стадию жизни морской свинки по возрасту.
     *
     * @param ageInYears возраст морской свинки в годах (должен быть > 0).
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     */
    fun determine(ageInYears: Double): LifeStage.GuineaPig {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        return when {
            ageInYears >= GERIATRIC_START -> LifeStage.GuineaPig.Geriatric
            ageInYears >= SENIOR_START -> LifeStage.GuineaPig.Senior
            ageInYears >= ADULT_START -> LifeStage.GuineaPig.Adult
            ageInYears >= JUVENILE_START -> LifeStage.GuineaPig.Juvenile
            else -> LifeStage.GuineaPig.Pup
        }
    }

    /**
     * Возвращает ожидаемый диапазон продолжительности жизни морской свинки (в годах).
     *
     * По Oxbow / Animallama (§4.5): ЧЖ 5–7 лет (подкатегорий нет).
     */
    fun expectedLifespanRange(): ClosedFloatingPointRange<Double> = 5.0..7.0

    /** Возраст начала Juvenile (~3 недели, отъём). */
    internal const val JUVENILE_START: Double = 0.058

    /** Возраст начала Adult (5 месяцев, физическая зрелость). */
    internal const val ADULT_START: Double = 0.42

    /** Возраст начала Senior (4 года, §4.5). */
    internal const val SENIOR_START: Double = 4.0

    /** Возраст начала Geriatric (6 лет). */
    internal const val GERIATRIC_START: Double = 6.0
}
