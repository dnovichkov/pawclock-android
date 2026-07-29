// Числовой литерал 80.0 — «эталонная» человеческая продолжительность жизни, к которой
// нормируется скалярная формула §4.8/§4.9/§4.11. Имеет фиксированный физиологический смысл.
@file:Suppress("detekt:MagicNumber")

package app.pawclock.calculator

/**
 * Скалярная формула «возраст в человеческих годах» для видов, где старение моделируется
 * простым отношением к видовой продолжительности жизни (Bird §4.8, Reptile §4.9, Fish §4.11).
 *
 * ```
 * ЧГ = age · 80 / lifespan
 * ```
 *
 * Идея: 80 лет — условная человеческая ЧЖ; питомец, проживший долю `age / lifespan` своей
 * жизни, «соответствует» той же доле человеческой жизни (× 80). Так вид с ЧЖ 80 лет даёт
 * `ЧГ = age` (1:1), короткоживущие виды стареют быстрее, долгоживущие — медленнее.
 *
 * Общий helper устраняет дублирование между [BirdAgeCalculator], `ReptileAgeCalculator`
 * (Task 8) и `FishAgeCalculator` (Task 10) и даёт единую точку для property-инварианта
 * `compute(age, lifespan) / age == 80 / lifespan`.
 *
 * Источники: AAV «Care for Senior Parrots» (§4.8), PetPlace / Reptile Centre (§4.9),
 * PetMD / AquariumStoreDepot (§4.11). См. ADR-0006.
 */
internal object ScalarRatioFormula {
    /** Эталонная человеческая продолжительность жизни (лет), к которой нормируется отношение. */
    const val HUMAN_REFERENCE_LIFESPAN: Double = 80.0

    /**
     * Возвращает возраст в человеческих годах по скалярному отношению `age · 80 / lifespan`.
     *
     * @param age календарный возраст питомца в годах.
     * @param lifespan видовая средняя продолжительность жизни в годах (должна быть > 0).
     * @throws IllegalArgumentException если `lifespan <= 0`.
     */
    fun compute(
        age: Double,
        lifespan: Double,
    ): Double {
        require(lifespan > 0) { "Lifespan must be positive, got $lifespan" }
        return age * HUMAN_REFERENCE_LIFESPAN / lifespan
    }
}
