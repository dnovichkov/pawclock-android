// Числовые литералы здесь — табличные пороги стадий жизни хомяков (RVC + Animallama).
// Каждое значение имеет ветеринарный смысл (возраст в годах перехода между стадиями),
// все собраны в KDoc-таблице ниже с указанием первоисточника.
@file:Suppress("detekt:MagicNumber")

package app.pawclock.calculator

import app.pawclock.model.HamsterType
import app.pawclock.model.LifeStage
import app.pawclock.model.Species

/**
 * Калькулятор стадии жизни хомяка по возрасту (RVC VetCompass + Animallama, §4.4 спецификации).
 *
 * | Стадия      | Возраст начала            |
 * |-------------|---------------------------|
 * | Pup         | 0 — ~3 нед. (0–0.0575)    |
 * | Juvenile    | ~3 нед. — 2 мес. (0.0575–0.1667) |
 * | Adult       | 2 мес. — 1 год (0.1667–1.0) |
 * | Senior      | 1 — 1.5 года              |
 * | VerySenior  | 1.5+ года («старость с 1.5 лет», §4.4) |
 *
 * Пороги едины для всех видов ([HamsterType] влияет только на
 * [expectedLifespanRange], но не на границы стадий).
 *
 * Источники:
 *  - Royal Veterinary College (RVC). *VetCompass — Hamster health and longevity*.
 *  - Animallama. *Hamster life stages*.
 *
 * См. также спецификацию PawClock §4.4 и ADR-0006.
 */
data object HamsterLifeStageCalculator : LifeStageCalculator {
    override val species: Species = Species.Hamster

    /**
     * Унифицированная точка диспатча из [LifeStageCalculator]: делегирует в перегрузку
     * [determine] с явным `type`.
     *
     * @throws IllegalArgumentException если [params] не является [SpeciesParams.Hamster].
     */
    override fun determine(
        ageInYears: Double,
        params: SpeciesParams,
    ): LifeStage {
        require(params is SpeciesParams.Hamster) {
            "HamsterLifeStageCalculator requires SpeciesParams.Hamster, got ${params::class.simpleName}"
        }
        return determine(ageInYears, params.type)
    }

    /**
     * Возвращает стадию жизни хомяка по возрасту.
     *
     * @param ageInYears возраст хомяка в годах (должен быть > 0).
     * @param type вид хомяка — на границы стадий не влияет, принимается для единообразия API.
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     */
    @Suppress("UNUSED_PARAMETER")
    fun determine(
        ageInYears: Double,
        type: HamsterType,
    ): LifeStage.Hamster {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        return when {
            ageInYears >= VERY_SENIOR_START -> LifeStage.Hamster.VerySenior
            ageInYears >= SENIOR_START -> LifeStage.Hamster.Senior
            ageInYears >= ADULT_START -> LifeStage.Hamster.Adult
            ageInYears >= JUVENILE_START -> LifeStage.Hamster.Juvenile
            else -> LifeStage.Hamster.Pup
        }
    }

    /**
     * Возвращает ожидаемый диапазон продолжительности жизни (в годах) для вида хомяка.
     *
     * По RVC / Animallama (§4.4): ЧЖ 1.5–3.5 года в зависимости от вида. Roborovski живут
     * дольше всех, карликовые (Dwarf/WinterWhite) — меньше.
     *
     * @param type вид хомяка.
     */
    fun expectedLifespanRange(type: HamsterType): ClosedFloatingPointRange<Double> =
        when (type) {
            HamsterType.Syrian, HamsterType.Chinese -> 2.0..3.0
            HamsterType.Roborovski -> 3.0..3.5
            HamsterType.Dwarf, HamsterType.WinterWhite -> 1.5..2.0
        }

    /** Возраст начала Juvenile (~3 недели, отъём). */
    internal const val JUVENILE_START: Double = 0.0575

    /** Возраст начала Adult (2 месяца). */
    internal const val ADULT_START: Double = 0.16667

    /** Возраст начала Senior (1 год). */
    internal const val SENIOR_START: Double = 1.0

    /** Возраст начала VerySenior (1.5 года). */
    internal const val VERY_SENIOR_START: Double = 1.5
}
