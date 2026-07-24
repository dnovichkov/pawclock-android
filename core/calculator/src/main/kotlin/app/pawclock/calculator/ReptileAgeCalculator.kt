package app.pawclock.calculator

import app.pawclock.model.ReptileType
import app.pawclock.model.Species

/**
 * Калькулятор возраста рептилии в человеческих годах (PetPlace + Reptile Centre + A-Z Animals,
 * см. §4.9 спецификации).
 *
 * Базируется на скалярной формуле [ScalarRatioFormula] `ЧГ = age · 80 / lifespan`, где
 * `lifespan` — видовая ЧЖ ([ReptileType.averageLifespanYears]). В отличие от птицы (§4.8)
 * поправка для молодняка не применяется — рептилии не демонстрируют резкого ускоренного
 * взросления в первые месяцы.
 *
 * Опорные точки (из §4.9):
 *  - Box/sea turtle (ЧЖ 40): 5 л. = 10 ЧГ
 *  - Corn snake (ЧЖ 20): 3 г. = 12
 *  - Green iguana (ЧЖ 20): 5 л. = 20
 *  - Leopard gecko (ЧЖ 15): 2 г. ≈ 10.7
 *
 * Источники:
 *  - PetPlace. *How to Tell the Age of Your Reptile.*
 *  - Reptile Centre. *Reptile Lifespan Guide.*
 *  - A-Z Animals. *Reptile Life Expectancy.*
 *
 * См. также спецификацию PawClock §4.9 и ADR-0006.
 */
data object ReptileAgeCalculator : AgeCalculator {
    override val species: Species = Species.Reptile

    /**
     * Унифицированная точка диспатча из [AgeCalculator]: делегирует в перегрузку с [ReptileType].
     *
     * @throws IllegalArgumentException если [params] не является [SpeciesParams.Reptile].
     */
    override fun toHumanYears(
        ageInYears: Double,
        params: SpeciesParams,
    ): Double {
        require(params is SpeciesParams.Reptile) {
            "ReptileAgeCalculator requires SpeciesParams.Reptile, got ${params::class.simpleName}"
        }
        return toHumanYears(ageInYears, params.type)
    }

    /**
     * Возвращает возраст рептилии в человеческих годах по скалярной формуле §4.9.
     *
     * @param ageInYears возраст рептилии в годах (должен быть > 0).
     * @param type вид рептилии — задаёт видовую ЧЖ для масштабирования формулы.
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     */
    fun toHumanYears(
        ageInYears: Double,
        type: ReptileType,
    ): Double {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        return ScalarRatioFormula.compute(ageInYears, type.averageLifespanYears)
    }
}
