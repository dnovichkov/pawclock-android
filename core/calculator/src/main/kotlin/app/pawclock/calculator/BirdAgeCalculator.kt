// Числовые литералы — параметры скалярной формулы птицы (§4.8): порог «молодой особи»
// 0.5 года и поправочный множитель 1.3. Оба имеют биологический смысл (ускоренное
// взросление в первые месяцы), описаны в KDoc ниже со ссылкой на первоисточник.
@file:Suppress("detekt:MagicNumber")

package app.pawclock.calculator

import app.pawclock.model.BirdType
import app.pawclock.model.Species

/**
 * Калькулятор возраста птицы в человеческих годах (AAV + Lafeber Vet, см. §4.8 спецификации).
 *
 * Базируется на скалярной формуле [ScalarRatioFormula] `ЧГ = age · 80 / lifespan`, где
 * `lifespan` — видовая ЧЖ ([BirdType.averageLifespanYears]). Для птенцов младше 6 месяцев
 * результат умножается на [JUVENILE_MULTIPLIER] = 1.3, отражая ускоренное физиологическое
 * взросление в первые месяцы жизни (§4.8).
 *
 * Опорные точки (из §4.8):
 *  - Budgerigar (ЧЖ 7): 1 г. ≈ 11.4 ЧГ; 3 г. ≈ 34.3
 *  - Cockatiel (ЧЖ 15): 5 л. ≈ 26.7
 *  - Macaw / Amazon (ЧЖ 50): 10 л. = 16
 *  - Canary (ЧЖ 10): 2 г. = 16
 *  - Budgerigar в 6 недель (0.115 г.): ≈ 1.7 ЧГ (× 1.3 поправка)
 *
 * Источники:
 *  - Association of Avian Veterinarians (AAV). *Care for Senior Parrots.*
 *  - Lafeber Veterinary. *Avian Life Stages.*
 *
 * См. также спецификацию PawClock §4.8 и ADR-0006.
 */
data object BirdAgeCalculator : AgeCalculator {
    override val species: Species = Species.Bird

    /**
     * Унифицированная точка диспатча из [AgeCalculator]: делегирует в перегрузку с явным [BirdType].
     *
     * @throws IllegalArgumentException если [params] не является [SpeciesParams.Bird].
     */
    override fun toHumanYears(
        ageInYears: Double,
        params: SpeciesParams,
    ): Double {
        require(params is SpeciesParams.Bird) {
            "BirdAgeCalculator requires SpeciesParams.Bird, got ${params::class.simpleName}"
        }
        return toHumanYears(ageInYears, params.type)
    }

    /**
     * Возвращает возраст птицы в человеческих годах по скалярной формуле §4.8.
     *
     * @param ageInYears возраст птицы в годах (должен быть > 0).
     * @param type вид птицы — задаёт видовую ЧЖ для масштабирования формулы.
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     */
    fun toHumanYears(
        ageInYears: Double,
        type: BirdType,
    ): Double {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        val base = ScalarRatioFormula.compute(ageInYears, type.averageLifespanYears)
        return if (ageInYears < JUVENILE_AGE_THRESHOLD) base * JUVENILE_MULTIPLIER else base
    }

    /** Возраст (годы), ниже которого применяется поправка ускоренного взросления (6 месяцев). */
    internal const val JUVENILE_AGE_THRESHOLD: Double = 0.5

    /** Поправочный множитель для птенцов младше 6 месяцев (§4.8). */
    internal const val JUVENILE_MULTIPLIER: Double = 1.3
}
