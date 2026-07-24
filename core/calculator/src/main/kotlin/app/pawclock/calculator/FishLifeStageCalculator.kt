// Числовые литералы — пороговые доли от видовой ЧЖ (PetMD + AquariumStoreDepot, §4.11). Каждое
// значение — граница стадии как процент прожитой жизни, описаны в KDoc-таблице ниже.
@file:Suppress("detekt:MagicNumber")

package app.pawclock.calculator

import app.pawclock.model.FishType
import app.pawclock.model.LifeStage
import app.pawclock.model.Species

/**
 * Калькулятор стадии жизни рыбы по возрасту (PetMD + Kodama Koi Farm + AquariumStoreDepot, §4.11).
 *
 * Как и у птицы/рептилии, границы стадий заданы **долей от видовой ЧЖ**
 * ([FishType.averageLifespanYears]), а не абсолютным возрастом — взросление гуппи (ЧЖ 2) и кои
 * (ЧЖ 30) наступает в очень разном календарном возрасте. В отличие от млекопитающих — 4 фазы.
 *
 * | Стадия    | Доля прожитой жизни |
 * |-----------|---------------------|
 * | Fry       | 0 — 5 %             |
 * | Juvenile  | 5 — 20 %           |
 * | Adult     | 20 — 75 %          |
 * | Senior    | 75 %+              |
 *
 * Источники:
 *  - PetMD. *How Long Do Fish Live?*
 *  - AquariumStoreDepot. *Aquarium Fish Lifespan Guide.*
 *
 * См. также спецификацию PawClock §4.11 и ADR-0006.
 */
data object FishLifeStageCalculator : LifeStageCalculator {
    override val species: Species = Species.Fish

    /**
     * Унифицированная точка диспатча из [LifeStageCalculator]: делегирует в перегрузку с [FishType].
     *
     * @throws IllegalArgumentException если [params] не является [SpeciesParams.Fish].
     */
    override fun determine(
        ageInYears: Double,
        params: SpeciesParams,
    ): LifeStage {
        require(params is SpeciesParams.Fish) {
            "FishLifeStageCalculator requires SpeciesParams.Fish, got ${params::class.simpleName}"
        }
        return determine(ageInYears, params.type)
    }

    /**
     * Возвращает стадию жизни рыбы по доле прожитой жизни `age / averageLifespan`.
     *
     * @param ageInYears возраст рыбы в годах (должен быть > 0).
     * @param type вид рыбы — задаёт видовую ЧЖ для нормировки.
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     */
    fun determine(
        ageInYears: Double,
        type: FishType,
    ): LifeStage.Fish {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        val fraction = ageInYears / type.averageLifespanYears
        return when {
            fraction >= SENIOR_FRACTION -> LifeStage.Fish.Senior
            fraction >= ADULT_FRACTION -> LifeStage.Fish.Adult
            fraction >= JUVENILE_FRACTION -> LifeStage.Fish.Juvenile
            else -> LifeStage.Fish.Fry
        }
    }

    /**
     * Возвращает ожидаемый диапазон продолжительности жизни (в годах) для вида рыбы.
     *
     * По PetMD / AquariumStoreDepot (§4.11): от 1–3 лет (гуппи) до 35+ лет (кои).
     *
     * @param type вид рыбы.
     */
    fun expectedLifespanRange(type: FishType): ClosedFloatingPointRange<Double> =
        when (type) {
            FishType.Guppy -> 1.0..3.0
            FishType.Betta, FishType.Tropical -> 3.0..7.0
            FishType.NeonTetra -> 5.0..10.0
            FishType.AngelFish, FishType.Discus -> 8.0..15.0
            FishType.Goldfish -> 10.0..20.0
            FishType.Koi -> 25.0..40.0
        }

    /** Верхняя граница Fry (5 % прожитой жизни). */
    internal const val JUVENILE_FRACTION: Double = 0.05

    /** Верхняя граница Juvenile (20 %). */
    internal const val ADULT_FRACTION: Double = 0.20

    /** Верхняя граница Adult (75 %). */
    internal const val SENIOR_FRACTION: Double = 0.75
}
