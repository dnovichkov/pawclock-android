// Числовые литералы здесь — опорные точки 3-фазной формулы лошади (AAEP + §4.10).
// Каждое значение имеет ветеринарный смысл (ЧГ в опорной точке либо темп старения),
// все собраны в KDoc-таблице ниже с указанием первоисточника.
@file:Suppress("detekt:MagicNumber")

package app.pawclock.calculator

import app.pawclock.model.Species

/**
 * Калькулятор возраста лошади в человеческих годах (AAEP, см. §4.10 спецификации).
 *
 * 3-фазная кусочно-линейная формула AAEP (age в годах; едина для всех типов —
 * порода влияет только на оценку ЧЖ), привязанная к опорным точкам:
 * ```
 * age ≤ 1        → 6.5 · age              // линейно 0 → 6.5 ЧГ за первый год
 * 1 < age ≤ 2    → 6.5 + 6.5 · (age − 1)  // +6.5 ЧГ за второй год → 13 ЧГ
 * 2 < age ≤ 3    → 13 + 5 · (age − 2)     // +5 ЧГ за третий год → 18 ЧГ
 * age > 3        → 18 + 2.5 · (age − 3)    // далее +2.5 ЧГ/год (фазы 4-й год и >4 лет совпадают)
 * ```
 *
 * Опорные точки (из §4.10): 1 г. = 6.5 ЧГ, 2 г. = 13, 3 г. = 18, 4 г. = 20.5, 10 л. = 35.5, 25 л. = 73.
 *
 * Сегменты «4-й год» (+2.5) и «>4 лет» (+2.5 ЧГ/год) из §4.10 имеют одинаковый наклон,
 * поэтому объединены в один сегмент `age > 3`. Формула **непрерывна** на всех стыках
 * (1/2/3 года) и монотонно возрастает на всём диапазоне.
 *
 * Источники:
 *  - American Association of Equine Practitioners (AAEP). *Vaccination Guidelines; Senior Horse Care.*
 *  - Schraer, K. (DVM). *Horse Lifespan.* PetMD.
 *
 * См. также спецификацию PawClock §4.10 и ADR-0006.
 */
data object HorseAgeCalculator : AgeCalculator {
    override val species: Species = Species.Horse

    /**
     * Унифицированная точка диспатча из [AgeCalculator]: делегирует в перегрузку без подкатегории.
     *
     * @throws IllegalArgumentException если [params] не является [SpeciesParams.Horse].
     */
    override fun toHumanYears(
        ageInYears: Double,
        params: SpeciesParams,
    ): Double {
        require(params is SpeciesParams.Horse) {
            "HorseAgeCalculator requires SpeciesParams.Horse, got ${params::class.simpleName}"
        }
        return toHumanYears(ageInYears)
    }

    /**
     * Возвращает возраст лошади в человеческих годах по 3-фазной формуле §4.10.
     *
     * @param ageInYears возраст лошади в годах (должен быть > 0).
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     */
    fun toHumanYears(ageInYears: Double): Double {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        return when {
            ageInYears <= ONE_YEAR -> RATE_FIRST_YEAR * ageInYears
            ageInYears <= TWO_YEARS ->
                HUMAN_YEARS_AT_ONE_YEAR + RATE_SECOND_YEAR * (ageInYears - ONE_YEAR)
            ageInYears <= THREE_YEARS ->
                HUMAN_YEARS_AT_TWO_YEARS + RATE_THIRD_YEAR * (ageInYears - TWO_YEARS)
            else -> HUMAN_YEARS_AT_THREE_YEARS + RATE_AFTER_THREE_YEARS * (ageInYears - THREE_YEARS)
        }
    }

    /** Граница первого сегмента — 1 год. */
    private const val ONE_YEAR: Double = 1.0

    /** Граница второго сегмента — 2 года. */
    private const val TWO_YEARS: Double = 2.0

    /** Граница третьего сегмента — 3 года. */
    private const val THREE_YEARS: Double = 3.0

    /** Темп первого года: 6.5 ЧГ за год (линейно 0 → 6.5). */
    private const val RATE_FIRST_YEAR: Double = 6.5

    /** ЧГ в опорной точке 1 год. */
    private const val HUMAN_YEARS_AT_ONE_YEAR: Double = 6.5

    /** Темп второго года: +6.5 ЧГ. */
    private const val RATE_SECOND_YEAR: Double = 6.5

    /** ЧГ в опорной точке 2 года. */
    private const val HUMAN_YEARS_AT_TWO_YEARS: Double = 13.0

    /** Темп третьего года: +5 ЧГ. */
    private const val RATE_THIRD_YEAR: Double = 5.0

    /** ЧГ в опорной точке 3 года. */
    private const val HUMAN_YEARS_AT_THREE_YEARS: Double = 18.0

    /** Темп после 3 лет: +2.5 ЧГ/год (фазы 4-й год и >4 лет, §4.10). */
    private const val RATE_AFTER_THREE_YEARS: Double = 2.5
}
