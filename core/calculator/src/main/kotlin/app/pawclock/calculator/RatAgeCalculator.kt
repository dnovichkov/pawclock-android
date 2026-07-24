package app.pawclock.calculator

import app.pawclock.model.Species

/**
 * Калькулятор возраста крысы в человеческих годах по Sengupta 2013
 * (см. §4.6 спецификации PawClock).
 *
 * Линейная формула (age в годах; едина для всех — у крысы нет подкатегорий):
 * ```
 * ЧГ = 13.8 · age + 1.4
 * ```
 *
 * Опорные точки (из §4.6): 0.5 г. = 8.3 ЧГ, 1 г. = 15.2, 2 г. = 29.0, 3 г. = 42.8.
 *
 * Коэффициент 13.8 ЧГ/год отражает, что 1 крысиный год ≈ 13.8 человеческих;
 * смещение 1.4 — поправка на ускоренное взросление в раннем возрасте. Функция
 * строго линейна и монотонно возрастает на всём диапазоне.
 *
 * Источник:
 *  - Sengupta, P. (2013). *The Laboratory Rat: Relating Its Age With Human's.*
 *    International Journal of Preventive Medicine, 4(6), 624–630. PMC3733029.
 *
 * См. также спецификацию PawClock §4.6 и ADR-0006.
 */
data object RatAgeCalculator : AgeCalculator {
    override val species: Species = Species.Rat

    /**
     * Унифицированная точка диспатча из [AgeCalculator]: делегирует в перегрузку без подкатегории.
     *
     * @throws IllegalArgumentException если [params] не является [SpeciesParams.Rat].
     */
    override fun toHumanYears(
        ageInYears: Double,
        params: SpeciesParams,
    ): Double {
        require(params is SpeciesParams.Rat) {
            "RatAgeCalculator requires SpeciesParams.Rat, got ${params::class.simpleName}"
        }
        return toHumanYears(ageInYears)
    }

    /**
     * Возвращает возраст крысы в человеческих годах по линейной формуле Sengupta 2013.
     *
     * @param ageInYears возраст крысы в годах (должен быть > 0).
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     */
    fun toHumanYears(ageInYears: Double): Double {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        return RAT_COEFFICIENT * ageInYears + RAT_OFFSET
    }

    /** Коэффициент линейной формулы Sengupta 2013: 13.8 человеческих лет на 1 крысиный год. */
    internal const val RAT_COEFFICIENT: Double = 13.8

    /** Свободный член линейной формулы Sengupta 2013: 1.4 ЧГ. */
    internal const val RAT_OFFSET: Double = 1.4
}
