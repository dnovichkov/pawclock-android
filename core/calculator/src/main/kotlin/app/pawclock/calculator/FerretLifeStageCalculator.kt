// Числовые литералы здесь — табличные пороги стадий жизни хорьков (PMC «Senior Ferret» + §4.7).
// Каждое значение имеет ветеринарный смысл (возраст в годах перехода между стадиями),
// все собраны в KDoc-таблице ниже с указанием первоисточника.
@file:Suppress("detekt:MagicNumber")

package app.pawclock.calculator

import app.pawclock.model.LifeStage
import app.pawclock.model.Species

/**
 * Калькулятор стадии жизни хорька по возрасту (PMC «Senior Ferret» PMC7129291, §4.7 спецификации).
 *
 * | Стадия    | Возраст начала                          |
 * |-----------|-----------------------------------------|
 * | Kit       | 0 — ~4 мес. (0–0.33, до отъёма)         |
 * | Juvenile  | ~4 мес. — 1 год (0.33–1.0)              |
 * | Adult     | 1 — 3 года (1.0–3.0)                    |
 * | Senior    | 3 — 5 лет («senior с 3–4 лет», §4.7)    |
 * | Geriatric | 5+ лет (приближение к ЧЖ 5–10 лет)      |
 *
 * Пороги едины — у хорька нет подкатегорий.
 *
 * Источники:
 *  - Church, R. R. (обзор PMC7129291). *The Senior Ferret (Mustela putorius furo).*
 *    Veterinary Clinics of North America: Exotic Animal Practice.
 *  - Oxbow Animal Health. *Ferret Life Stages.*
 *
 * См. также спецификацию PawClock §4.7 и ADR-0006.
 */
data object FerretLifeStageCalculator : LifeStageCalculator {
    override val species: Species = Species.Ferret

    /**
     * Унифицированная точка диспатча из [LifeStageCalculator]: делегирует в перегрузку без подкатегории.
     *
     * @throws IllegalArgumentException если [params] не является [SpeciesParams.Ferret].
     */
    override fun determine(
        ageInYears: Double,
        params: SpeciesParams,
    ): LifeStage {
        require(params is SpeciesParams.Ferret) {
            "FerretLifeStageCalculator requires SpeciesParams.Ferret, got ${params::class.simpleName}"
        }
        return determine(ageInYears)
    }

    /**
     * Возвращает стадию жизни хорька по возрасту.
     *
     * @param ageInYears возраст хорька в годах (должен быть > 0).
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     */
    fun determine(ageInYears: Double): LifeStage.Ferret {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        return when {
            ageInYears >= GERIATRIC_START -> LifeStage.Ferret.Geriatric
            ageInYears >= SENIOR_START -> LifeStage.Ferret.Senior
            ageInYears >= ADULT_START -> LifeStage.Ferret.Adult
            ageInYears >= JUVENILE_START -> LifeStage.Ferret.Juvenile
            else -> LifeStage.Ferret.Kit
        }
    }

    /**
     * Возвращает ожидаемый диапазон продолжительности жизни хорька (в годах).
     *
     * По PMC «Senior Ferret» / §4.7: ЧЖ 5–10 лет (подкатегорий нет).
     */
    fun expectedLifespanRange(): ClosedFloatingPointRange<Double> = 5.0..10.0

    /** Возраст начала Juvenile (~4 месяца). */
    internal const val JUVENILE_START: Double = 0.33

    /** Возраст начала Adult (1 год). */
    internal const val ADULT_START: Double = 1.0

    /** Возраст начала Senior (3 года, §4.7). */
    internal const val SENIOR_START: Double = 3.0

    /** Возраст начала Geriatric (5 лет). */
    internal const val GERIATRIC_START: Double = 5.0
}
