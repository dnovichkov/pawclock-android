// Числовые литералы — пороговые доли от видовой ЧЖ (PetPlace + Reptile Centre, §4.9). Каждое
// значение — граница стадии как процент прожитой жизни, описаны в KDoc-таблице ниже.
@file:Suppress("detekt:MagicNumber")

package app.pawclock.calculator

import app.pawclock.model.LifeStage
import app.pawclock.model.ReptileType
import app.pawclock.model.Species

/**
 * Калькулятор стадии жизни рептилии по возрасту (PetPlace + Reptile Centre + A-Z Animals, §4.9).
 *
 * Как и у птицы, границы стадий заданы **долей от видовой ЧЖ**
 * ([ReptileType.averageLifespanYears]), а не абсолютным возрастом — взросление геккона (ЧЖ 15)
 * и черепахи (ЧЖ 40) наступает в очень разном календарном возрасте. В отличие от млекопитающих —
 * 4 фазы (§4.9).
 *
 * | Стадия    | Доля прожитой жизни |
 * |-----------|---------------------|
 * | Hatchling | 0 — 5 %             |
 * | Juvenile  | 5 — 20 %           |
 * | Adult     | 20 — 75 %          |
 * | Senior    | 75 %+              |
 *
 * Источники:
 *  - PetPlace. *How to Tell the Age of Your Reptile.*
 *  - Reptile Centre. *Reptile Lifespan Guide.*
 *
 * См. также спецификацию PawClock §4.9 и ADR-0006.
 */
data object ReptileLifeStageCalculator : LifeStageCalculator {
    override val species: Species = Species.Reptile

    /**
     * Унифицированная точка диспатча из [LifeStageCalculator]: делегирует в перегрузку с [ReptileType].
     *
     * @throws IllegalArgumentException если [params] не является [SpeciesParams.Reptile].
     */
    override fun determine(
        ageInYears: Double,
        params: SpeciesParams,
    ): LifeStage {
        require(params is SpeciesParams.Reptile) {
            "ReptileLifeStageCalculator requires SpeciesParams.Reptile, got ${params::class.simpleName}"
        }
        return determine(ageInYears, params.type)
    }

    /**
     * Возвращает стадию жизни рептилии по доле прожитой жизни `age / averageLifespan`.
     *
     * @param ageInYears возраст рептилии в годах (должен быть > 0).
     * @param type вид рептилии — задаёт видовую ЧЖ для нормировки.
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     */
    fun determine(
        ageInYears: Double,
        type: ReptileType,
    ): LifeStage.Reptile {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        val fraction = ageInYears / type.averageLifespanYears
        return when {
            fraction >= SENIOR_FRACTION -> LifeStage.Reptile.Senior
            fraction >= ADULT_FRACTION -> LifeStage.Reptile.Adult
            fraction >= JUVENILE_FRACTION -> LifeStage.Reptile.Juvenile
            else -> LifeStage.Reptile.Hatchling
        }
    }

    /**
     * Возвращает ожидаемый диапазон продолжительности жизни (в годах) для вида рептилии.
     *
     * По PetPlace / Reptile Centre (§4.9): от 10–20 лет (мелкие гекконы) до 40+ лет (черепахи).
     *
     * @param type вид рептилии.
     */
    fun expectedLifespanRange(type: ReptileType): ClosedFloatingPointRange<Double> =
        when (type) {
            ReptileType.LeopardGecko, ReptileType.CrestedGecko -> 10.0..20.0
            ReptileType.BeardedDragon -> 8.0..15.0
            ReptileType.CornSnake, ReptileType.GreenIguana -> 15.0..25.0
            ReptileType.BallPython, ReptileType.RedEaredSlider -> 20.0..40.0
            ReptileType.BoxTurtle -> 40.0..60.0
        }

    /** Верхняя граница Hatchling (5 % прожитой жизни). */
    internal const val JUVENILE_FRACTION: Double = 0.05

    /** Верхняя граница Juvenile (20 %). */
    internal const val ADULT_FRACTION: Double = 0.20

    /** Верхняя граница Adult (75 %). */
    internal const val SENIOR_FRACTION: Double = 0.75
}
