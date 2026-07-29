package app.pawclock.calculator

import app.pawclock.model.Species

/**
 * Калькулятор возраста морской свинки в человеческих годах по руководствам
 * Oxbow Animal Health + Animallama (см. §4.5 спецификации PawClock).
 *
 * Кусочная формула (age в годах; едина для всех — у морской свинки нет подкатегорий):
 * ```
 * age ≤ 0.058 (0–3 нед.)        → линейно 0 → 6        (отъём ≈ 6 ЧГ)
 * 0.058 < age ≤ 0.208 (3 нед.–2.5 мес.) → линейно 6 → 11.5  (половая зрелость)
 * 0.208 < age ≤ 0.42 (2.5–5 мес.)       → линейно 11.5 → 20  (физическая зрелость)
 * 0.42 < age ≤ 4.0 (5 мес.–4 года)      → 20 + 8 · (age − 0.42)
 * age > 4.0 (4+ года)                   → 48.64 + 10 · (age − 4)
 * ```
 *
 * Опорные точки (из §4.5): 3 нед. ≈ 6 ЧГ, 5 мес. = 20, 1 год ≈ 24.6, 4 года ≈ 48.6, 7 лет ≈ 78.6.
 *
 * **Примечание о границах.** В отличие от §4.3 (кролик), формула непрерывна на всех стыках:
 * каждый сегмент стартует ровно с конечного значения предыдущего (6, 11.5, 20, 48.64).
 * Функция строго монотонно возрастает. Скорость старения растёт после 4 лет (+10 вместо +8
 * ЧГ/год) — отражает «senior с 4 лет» из §4.5.
 *
 * Источники:
 *  - Oxbow Animal Health. *Guinea Pig Lifespan and Life Stages*.
 *  - Animallama. *Guinea pig age chart / How old is my guinea pig in human years*.
 *
 * См. также спецификацию PawClock §4.5 и ADR-0006.
 */
data object GuineaPigAgeCalculator : AgeCalculator {
    override val species: Species = Species.GuineaPig

    /**
     * Унифицированная точка диспатча из [AgeCalculator]: делегирует в перегрузку без подкатегории.
     *
     * @throws IllegalArgumentException если [params] не является [SpeciesParams.GuineaPig].
     */
    override fun toHumanYears(
        ageInYears: Double,
        params: SpeciesParams,
    ): Double {
        require(params is SpeciesParams.GuineaPig) {
            "GuineaPigAgeCalculator requires SpeciesParams.GuineaPig, got ${params::class.simpleName}"
        }
        return toHumanYears(ageInYears)
    }

    /**
     * Возвращает возраст морской свинки в человеческих годах.
     *
     * @param ageInYears возраст морской свинки в годах (должен быть > 0).
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     */
    fun toHumanYears(ageInYears: Double): Double {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        return when {
            ageInYears <= WEANING_AGE ->
                interpolate(ageInYears, 0.0, 0.0, WEANING_AGE, WEANING_HUMAN_AGE)
            ageInYears <= PUBERTY_AGE ->
                interpolate(ageInYears, WEANING_AGE, WEANING_HUMAN_AGE, PUBERTY_AGE, PUBERTY_HUMAN_AGE)
            ageInYears <= MATURITY_AGE ->
                interpolate(ageInYears, PUBERTY_AGE, PUBERTY_HUMAN_AGE, MATURITY_AGE, MATURITY_HUMAN_AGE)
            ageInYears <= SENIOR_AGE ->
                MATURITY_HUMAN_AGE + ADULT_RATE * (ageInYears - MATURITY_AGE)
            else ->
                SENIOR_HUMAN_AGE + SENIOR_RATE * (ageInYears - SENIOR_AGE)
        }
    }

    /** Линейная интерполяция между точками `(x0, y0)` и `(x1, y1)`. */
    private fun interpolate(
        x: Double,
        x0: Double,
        y0: Double,
        x1: Double,
        y1: Double,
    ): Double = y0 + (y1 - y0) / (x1 - x0) * (x - x0)

    // Константы кусочной формулы Oxbow / Animallama (§4.5).

    /** Возраст отъёма (≈ 3 недели). */
    internal const val WEANING_AGE: Double = 0.058

    /** Человеческий возраст на отъёме. */
    internal const val WEANING_HUMAN_AGE: Double = 6.0

    /** Возраст половой зрелости (≈ 2.5 месяца). */
    internal const val PUBERTY_AGE: Double = 0.208

    /** Человеческий возраст при половой зрелости. */
    internal const val PUBERTY_HUMAN_AGE: Double = 11.5

    /** Возраст физической зрелости (≈ 5 месяцев). */
    internal const val MATURITY_AGE: Double = 0.42

    /** Человеческий возраст при физической зрелости. */
    internal const val MATURITY_HUMAN_AGE: Double = 20.0

    /** Скорость старения взрослой свинки (5 мес.–4 года): 8 ЧГ/год. */
    internal const val ADULT_RATE: Double = 8.0

    /** Возраст начала senior (4 года, §4.5). */
    internal const val SENIOR_AGE: Double = 4.0

    /** Человеческий возраст в 4 года: 20 + 8 · (4 − 0.42) = 48.64. */
    internal const val SENIOR_HUMAN_AGE: Double = 48.64

    /** Скорость старения после 4 лет: 10 ЧГ/год. */
    internal const val SENIOR_RATE: Double = 10.0
}
