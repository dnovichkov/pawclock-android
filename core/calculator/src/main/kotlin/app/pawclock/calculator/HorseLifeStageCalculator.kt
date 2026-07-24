// Числовые литералы здесь — табличные пороги стадий жизни лошадей (AAEP + §4.10).
// Каждое значение имеет ветеринарный смысл (возраст в годах перехода между стадиями),
// все собраны в KDoc-таблице ниже с указанием первоисточника.
@file:Suppress("detekt:MagicNumber")

package app.pawclock.calculator

import app.pawclock.model.HorseType
import app.pawclock.model.LifeStage
import app.pawclock.model.Species

/**
 * Калькулятор стадии жизни лошади по возрасту (AAEP Senior Horse Care + PetMD, §4.10 спецификации).
 *
 * | Стадия     | Возраст начала                              |
 * |------------|---------------------------------------------|
 * | Foal       | 0 — 1 год (жеребёнок)                        |
 * | Yearling   | 1 — 2 года (годовик)                        |
 * | YoungAdult | 2 — 4 года (период заездки/созревания)      |
 * | Adult      | 4 — 15 лет (зрелость)                       |
 * | Senior     | 15+ лет (AAEP Senior Horse Care)            |
 *
 * Пороги едины — формула AAEP не зависит от породы; [HorseType] влияет только на оценку ЧЖ.
 *
 * Источники:
 *  - American Association of Equine Practitioners (AAEP). *Senior Horse Care.*
 *  - Schraer, K. (DVM). *Horse Lifespan.* PetMD.
 *
 * См. также спецификацию PawClock §4.10 и ADR-0006.
 */
data object HorseLifeStageCalculator : LifeStageCalculator {
    override val species: Species = Species.Horse

    /**
     * Унифицированная точка диспатча из [LifeStageCalculator]: делегирует в перегрузку без подкатегории.
     *
     * @throws IllegalArgumentException если [params] не является [SpeciesParams.Horse].
     */
    override fun determine(
        ageInYears: Double,
        params: SpeciesParams,
    ): LifeStage {
        require(params is SpeciesParams.Horse) {
            "HorseLifeStageCalculator requires SpeciesParams.Horse, got ${params::class.simpleName}"
        }
        return determine(ageInYears)
    }

    /**
     * Возвращает стадию жизни лошади по возрасту.
     *
     * @param ageInYears возраст лошади в годах (должен быть > 0).
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     */
    fun determine(ageInYears: Double): LifeStage.Horse {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        return when {
            ageInYears >= SENIOR_START -> LifeStage.Horse.Senior
            ageInYears >= ADULT_START -> LifeStage.Horse.Adult
            ageInYears >= YOUNG_ADULT_START -> LifeStage.Horse.YoungAdult
            ageInYears >= YEARLING_START -> LifeStage.Horse.Yearling
            else -> LifeStage.Horse.Foal
        }
    }

    /**
     * Возвращает ожидаемый диапазон продолжительности жизни лошади (в годах) по §4.10.
     *
     * База 25–30 лет; пони традиционно живут дольше, тяжеловозы — меньше.
     */
    fun expectedLifespanRange(type: HorseType): ClosedFloatingPointRange<Double> =
        when (type) {
            HorseType.Pony -> 25.0..35.0
            HorseType.DraftHorse -> 20.0..25.0
            HorseType.LightHorse, HorseType.Thoroughbred -> 25.0..30.0
        }

    /** Возраст начала Yearling (1 год). */
    internal const val YEARLING_START: Double = 1.0

    /** Возраст начала YoungAdult (2 года). */
    internal const val YOUNG_ADULT_START: Double = 2.0

    /** Возраст начала Adult (4 года). */
    internal const val ADULT_START: Double = 4.0

    /** Возраст начала Senior (15 лет, AAEP Senior Horse Care). */
    internal const val SENIOR_START: Double = 15.0
}
