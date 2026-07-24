package app.pawclock.calculator

import app.pawclock.model.RabbitSize
import app.pawclock.model.Species

/**
 * Калькулятор возраста кролика в человеческих годах по руководствам
 * House Rabbit Society + AVMA (см. §4.3 спецификации PawClock).
 *
 * Кусочная формула (едина для всех пород — размер на возраст не влияет):
 * ```
 * age < 0.33 (0–4 мес.)      → 30 · age
 * 0.33 ≤ age < 1 (4–12 мес.) → 12 + 8 · (age − 0.33)
 * age ≥ 1 (1+ год)           → 21 + 6 · (age − 1)
 * ```
 *
 * Опорные точки (из §4.3): 1 год = 21 ЧГ, 2 года = 27, 5 лет = 45, 8 лет = 63.
 *
 * **Примечание о границах.** Формула привязана к опорным точкам начала сегментов
 * (4 мес. → 12 ЧГ, 1 год → 21 ЧГ), поэтому на стыках 0.33 и 1.0 имеется небольшой
 * скачок вверх (≈+2 и ≈+3.6 ЧГ). Функция строго монотонно возрастает. Граница
 * 0.33 принадлежит второму сегменту, граница 1.0 — третьему.
 *
 * Источники:
 *  - House Rabbit Society. *Rabbit Life Stages / How Old Is My Rabbit*.
 *  - American Veterinary Medical Association (AVMA), руководства по содержанию кроликов.
 *
 * См. также спецификацию PawClock §4.3 и ADR-0006.
 */
data object RabbitAgeCalculator : AgeCalculator {
    override val species: Species = Species.Rabbit

    /**
     * Унифицированная точка диспатча из [AgeCalculator]: делегирует в перегрузку
     * [toHumanYears] с явным `size`.
     *
     * @throws IllegalArgumentException если [params] не является [SpeciesParams.Rabbit].
     */
    override fun toHumanYears(
        ageInYears: Double,
        params: SpeciesParams,
    ): Double {
        require(params is SpeciesParams.Rabbit) {
            "RabbitAgeCalculator requires SpeciesParams.Rabbit, got ${params::class.simpleName}"
        }
        return toHumanYears(ageInYears, params.size)
    }

    /**
     * Возвращает возраст кролика в человеческих годах.
     *
     * @param ageInYears возраст кролика в годах (должен быть > 0).
     * @param size порода/размер — на формулу не влияет, принимается для единообразия API.
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     */
    @Suppress("UNUSED_PARAMETER")
    fun toHumanYears(
        ageInYears: Double,
        size: RabbitSize,
    ): Double {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        return when {
            ageInYears < FOUR_MONTHS ->
                INFANT_RATE * ageInYears
            ageInYears < ONE_YEAR ->
                FOUR_MONTHS_HUMAN_AGE + MID_RATE * (ageInYears - FOUR_MONTHS)
            else ->
                ONE_YEAR_HUMAN_AGE + ADULT_RATE * (ageInYears - ONE_YEAR)
        }
    }

    // Константы кусочной формулы House Rabbit Society / AVMA.

    /** Граница «0–4 мес.» в годах (≈ 4 месяца). */
    internal const val FOUR_MONTHS: Double = 0.33

    /** Граница «1 год». */
    internal const val ONE_YEAR: Double = 1.0

    /** Скорость старения в первом сегменте (0–4 мес.): 30 ЧГ/год. */
    internal const val INFANT_RATE: Double = 30.0

    /** Человеческий возраст в 4 месяца (опорная точка второго сегмента). */
    internal const val FOUR_MONTHS_HUMAN_AGE: Double = 12.0

    /** Скорость старения во втором сегменте (4–12 мес.): 8 ЧГ/год. */
    internal const val MID_RATE: Double = 8.0

    /** Человеческий возраст в 1 год (опорная точка третьего сегмента). */
    internal const val ONE_YEAR_HUMAN_AGE: Double = 21.0

    /** Скорость старения после 1 года: 6 ЧГ/год. */
    internal const val ADULT_RATE: Double = 6.0
}
