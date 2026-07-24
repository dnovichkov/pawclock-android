package app.pawclock.calculator

import app.pawclock.model.HamsterType
import app.pawclock.model.Species

/**
 * Калькулятор возраста хомяка в человеческих годах по руководствам
 * RVC VetCompass + Animallama (см. §4.4 спецификации PawClock).
 *
 * Кусочная формула (расчёт в месяцах, `m = ageInYears · 12`; едина для всех видов —
 * [HamsterType] на возраст не влияет):
 * ```
 * m ≤ 1 (0–1 мес.)      → 7.5 · m              (1 ЧГ за каждые 4 дня, 30-дневный месяц)
 * 1 < m ≤ 2 (1–2 мес.)  → 7.5 + 10.5 · (m − 1) (→ 18 ЧГ на 2 мес.)
 * 2 < m ≤ 18 (2–18 мес.)→ 18 + 3 · (m − 2)     (≈+3 ЧГ/мес. → 30 на 6 мес., 66 на 18 мес.)
 * m > 18 (18+ мес.)     → 66 + 4 · (m − 18)    (+4 ЧГ/мес.)
 * ```
 *
 * Опорные точки (из §4.4): 6 мес. = 30 ЧГ, 12 мес. ≈ 48, 18 мес. = 66, 24 мес. = 90.
 *
 * **Примечание о границах.** В отличие от §4.3 (кролик), формула непрерывна на всех стыках
 * (1, 2, 18 мес.) — сегменты подобраны так, чтобы значения совпадали. Функция строго
 * монотонно возрастает. Сегменты 2–6 мес. и 6–18 мес. из спецификации объединены в один
 * (одинаковая скорость +3 ЧГ/мес.).
 *
 * Источники:
 *  - Royal Veterinary College (RVC). *VetCompass — Hamster health and longevity*.
 *  - Animallama. *Hamster age chart / How old is my hamster in human years*.
 *
 * См. также спецификацию PawClock §4.4 и ADR-0006.
 */
data object HamsterAgeCalculator : AgeCalculator {
    override val species: Species = Species.Hamster

    /**
     * Унифицированная точка диспатча из [AgeCalculator]: делегирует в перегрузку
     * [toHumanYears] с явным `type`.
     *
     * @throws IllegalArgumentException если [params] не является [SpeciesParams.Hamster].
     */
    override fun toHumanYears(
        ageInYears: Double,
        params: SpeciesParams,
    ): Double {
        require(params is SpeciesParams.Hamster) {
            "HamsterAgeCalculator requires SpeciesParams.Hamster, got ${params::class.simpleName}"
        }
        return toHumanYears(ageInYears, params.type)
    }

    /**
     * Возвращает возраст хомяка в человеческих годах.
     *
     * @param ageInYears возраст хомяка в годах (должен быть > 0).
     * @param type вид хомяка — на формулу не влияет, принимается для единообразия API.
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     */
    @Suppress("UNUSED_PARAMETER")
    fun toHumanYears(
        ageInYears: Double,
        type: HamsterType,
    ): Double {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        val months = ageInYears * MONTHS_PER_YEAR
        return when {
            months <= ONE_MONTH ->
                INFANT_RATE * months
            months <= TWO_MONTHS ->
                INFANT_HUMAN_AGE + JUVENILE_RATE * (months - ONE_MONTH)
            months <= EIGHTEEN_MONTHS ->
                TWO_MONTHS_HUMAN_AGE + ADULT_RATE * (months - TWO_MONTHS)
            else ->
                EIGHTEEN_MONTHS_HUMAN_AGE + SENIOR_RATE * (months - EIGHTEEN_MONTHS)
        }
    }

    // Константы кусочной формулы RVC VetCompass / Animallama.

    /** Число месяцев в году для конвертации `ageInYears → months`. */
    internal const val MONTHS_PER_YEAR: Double = 12.0

    /** Граница «1 месяц» (в месяцах). */
    internal const val ONE_MONTH: Double = 1.0

    /** Граница «2 месяца» (в месяцах). */
    internal const val TWO_MONTHS: Double = 2.0

    /** Граница «18 месяцев» (в месяцах). */
    internal const val EIGHTEEN_MONTHS: Double = 18.0

    /** Скорость старения в первом сегменте (0–1 мес.): 7.5 ЧГ/мес. (≈1 ЧГ за 4 дня). */
    internal const val INFANT_RATE: Double = 7.5

    /** Человеческий возраст в 1 месяц (опорная точка второго сегмента). */
    internal const val INFANT_HUMAN_AGE: Double = 7.5

    /** Скорость старения во втором сегменте (1–2 мес.): 10.5 ЧГ/мес. */
    internal const val JUVENILE_RATE: Double = 10.5

    /** Человеческий возраст в 2 месяца (опорная точка третьего сегмента). */
    internal const val TWO_MONTHS_HUMAN_AGE: Double = 18.0

    /** Скорость старения в третьем сегменте (2–18 мес.): 3 ЧГ/мес. */
    internal const val ADULT_RATE: Double = 3.0

    /** Человеческий возраст в 18 месяцев (опорная точка четвёртого сегмента). */
    internal const val EIGHTEEN_MONTHS_HUMAN_AGE: Double = 66.0

    /** Скорость старения после 18 мес.: 4 ЧГ/мес. */
    internal const val SENIOR_RATE: Double = 4.0
}
