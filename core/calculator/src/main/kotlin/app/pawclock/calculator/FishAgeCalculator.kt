package app.pawclock.calculator

import app.pawclock.model.FishType
import app.pawclock.model.Species

/**
 * Калькулятор возраста рыбы в человеческих годах (PetMD + Kodama Koi Farm + AquariumStoreDepot,
 * см. §4.11 спецификации).
 *
 * Базируется на скалярной формуле [ScalarRatioFormula] `ЧГ = age · 80 / lifespan`, где
 * `lifespan` — видовая ЧЖ ([FishType.averageLifespanYears]). Как и у рептилии (§4.9), поправка
 * для молодняка не применяется — модель старения рыб простая, линейная по доле прожитой жизни.
 *
 * Опорные точки (из §4.11):
 *  - Guppy (ЧЖ 2): 1 г. = 40 ЧГ
 *  - Betta (ЧЖ 5): 3 г. = 48
 *  - Goldfish (ЧЖ 15): 5 л. ≈ 26.7
 *  - Koi (ЧЖ 30): 10 л. ≈ 26.7
 *  - NeonTetra (ЧЖ 8): 2 г. = 20
 *
 * Источники:
 *  - PetMD. *How Long Do Fish Live?*
 *  - Kodama Koi Farm. *Koi Fish Lifespan.*
 *  - AquariumStoreDepot. *Aquarium Fish Lifespan Guide.*
 *
 * См. также спецификацию PawClock §4.11 и ADR-0006.
 */
data object FishAgeCalculator : AgeCalculator {
    override val species: Species = Species.Fish

    /**
     * Унифицированная точка диспатча из [AgeCalculator]: делегирует в перегрузку с [FishType].
     *
     * @throws IllegalArgumentException если [params] не является [SpeciesParams.Fish].
     */
    override fun toHumanYears(
        ageInYears: Double,
        params: SpeciesParams,
    ): Double {
        require(params is SpeciesParams.Fish) {
            "FishAgeCalculator requires SpeciesParams.Fish, got ${params::class.simpleName}"
        }
        return toHumanYears(ageInYears, params.type)
    }

    /**
     * Возвращает возраст рыбы в человеческих годах по скалярной формуле §4.11.
     *
     * @param ageInYears возраст рыбы в годах (должен быть > 0).
     * @param type вид рыбы — задаёт видовую ЧЖ для масштабирования формулы.
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     */
    fun toHumanYears(
        ageInYears: Double,
        type: FishType,
    ): Double {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        return ScalarRatioFormula.compute(ageInYears, type.averageLifespanYears)
    }
}
