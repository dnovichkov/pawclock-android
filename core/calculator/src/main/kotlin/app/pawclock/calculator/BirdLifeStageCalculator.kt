// Числовые литералы — пороговые доли от видовой ЧЖ (AAV + Lafeber Vet, §4.8). Каждое
// значение — граница стадии как процент прожитой жизни, описаны в KDoc-таблице ниже.
@file:Suppress("detekt:MagicNumber")

package app.pawclock.calculator

import app.pawclock.model.BirdType
import app.pawclock.model.LifeStage
import app.pawclock.model.Species

/**
 * Калькулятор стадии жизни птицы по возрасту (AAV «Care for Senior Parrots» + Lafeber Vet, §4.8).
 *
 * Границы стадий заданы **долей от видовой ЧЖ** ([BirdType.averageLifespanYears]), а не
 * абсолютным возрастом — взросление волнистого попугая (ЧЖ 7) и ара (ЧЖ 50) наступает в
 * очень разном календарном возрасте.
 *
 * | Стадия    | Доля прожитой жизни |
 * |-----------|---------------------|
 * | Hatchling | 0 — 5 %             |
 * | Juvenile  | 5 — 20 %           |
 * | Adult     | 20 — 70 %          |
 * | Senior    | 70 — 90 %          |
 * | Geriatric | 90 %+              |
 *
 * Источники:
 *  - Association of Avian Veterinarians (AAV). *Care for Senior Parrots.*
 *  - Lafeber Veterinary. *Avian Life Stages.*
 *
 * См. также спецификацию PawClock §4.8 и ADR-0006.
 */
data object BirdLifeStageCalculator : LifeStageCalculator {
    override val species: Species = Species.Bird

    /**
     * Унифицированная точка диспатча из [LifeStageCalculator]: делегирует в перегрузку с [BirdType].
     *
     * @throws IllegalArgumentException если [params] не является [SpeciesParams.Bird].
     */
    override fun determine(
        ageInYears: Double,
        params: SpeciesParams,
    ): LifeStage {
        require(params is SpeciesParams.Bird) {
            "BirdLifeStageCalculator requires SpeciesParams.Bird, got ${params::class.simpleName}"
        }
        return determine(ageInYears, params.type)
    }

    /**
     * Возвращает стадию жизни птицы по доле прожитой жизни `age / averageLifespan`.
     *
     * @param ageInYears возраст птицы в годах (должен быть > 0).
     * @param type вид птицы — задаёт видовую ЧЖ для нормировки.
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     */
    fun determine(
        ageInYears: Double,
        type: BirdType,
    ): LifeStage.Bird {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        val fraction = ageInYears / type.averageLifespanYears
        return when {
            fraction >= GERIATRIC_FRACTION -> LifeStage.Bird.Geriatric
            fraction >= SENIOR_FRACTION -> LifeStage.Bird.Senior
            fraction >= ADULT_FRACTION -> LifeStage.Bird.Adult
            fraction >= JUVENILE_FRACTION -> LifeStage.Bird.Juvenile
            else -> LifeStage.Bird.Hatchling
        }
    }

    /**
     * Возвращает ожидаемый диапазон продолжительности жизни (в годах) для вида птицы.
     *
     * По AAV / Lafeber (§4.8): от 5–10 лет (волнистые) до 40–60 лет (ара, какаду).
     *
     * @param type вид птицы.
     */
    fun expectedLifespanRange(type: BirdType): ClosedFloatingPointRange<Double> =
        when (type) {
            BirdType.Budgerigar -> 5.0..10.0
            BirdType.Canary, BirdType.Pigeon -> 7.0..12.0
            BirdType.Lovebird -> 8.0..15.0
            BirdType.Cockatiel -> 10.0..20.0
            BirdType.Conure -> 15.0..25.0
            BirdType.Amazon, BirdType.AfricanGrey -> 40.0..50.0
            BirdType.Cockatoo, BirdType.Macaw -> 40.0..60.0
        }

    /** Верхняя граница Hatchling (5 % прожитой жизни). */
    internal const val JUVENILE_FRACTION: Double = 0.05

    /** Верхняя граница Juvenile (20 %). */
    internal const val ADULT_FRACTION: Double = 0.20

    /** Верхняя граница Adult (70 %). */
    internal const val SENIOR_FRACTION: Double = 0.70

    /** Верхняя граница Senior (90 %). */
    internal const val GERIATRIC_FRACTION: Double = 0.90
}
