// Числовые литералы здесь — опорные точки кусочной формулы хорька (PMC «Senior Ferret» + §4.7).
// Каждое значение имеет ветеринарный смысл (ЧГ в опорной точке либо темп старения),
// все собраны в KDoc-таблице ниже с указанием первоисточника.
@file:Suppress("detekt:MagicNumber")

package app.pawclock.calculator

import app.pawclock.model.Species

/**
 * Калькулятор возраста хорька в человеческих годах (PMC «Senior Ferret», см. §4.7 спецификации).
 *
 * Кусочно-линейная формула (age в годах; едина для всех — у хорька нет подкатегорий),
 * привязанная к опорным точкам ветеринарных источников:
 * ```
 * age < 0.5 (6 мес.)   → 60 · age              // +5 ЧГ за каждый месяц до 6 мес.
 * 0.5 ≤ age < 1        → 30 + 20 · (age − 0.5) // линейно от 30 ЧГ (6 мес.) к 40 ЧГ (1 год)
 * age ≥ 1              → 40 + 4 · (age − 1)     // далее +4 ЧГ/год
 * ```
 *
 * Опорные точки (из §4.7): 2 мес. = 10 ЧГ, 6 мес. = 30, 1 г. = 40, 3 г. = 48, 7 л. = 64.
 *
 * Формула **непрерывна** на всех стыках (0.5 г. → 30 ЧГ в обоих сегментах; 1 г. → 40 ЧГ)
 * и монотонно возрастает на всём диапазоне.
 *
 * Источники:
 *  - Church, R. R. (2007/обзор PMC7129291). *The Senior Ferret (Mustela putorius furo).*
 *    Veterinary Clinics of North America: Exotic Animal Practice. PMC7129291.
 *  - Oxbow Animal Health. *Ferret Life Stages.*
 *
 * См. также спецификацию PawClock §4.7 и ADR-0006.
 */
data object FerretAgeCalculator : AgeCalculator {
    override val species: Species = Species.Ferret

    /**
     * Унифицированная точка диспатча из [AgeCalculator]: делегирует в перегрузку без подкатегории.
     *
     * @throws IllegalArgumentException если [params] не является [SpeciesParams.Ferret].
     */
    override fun toHumanYears(
        ageInYears: Double,
        params: SpeciesParams,
    ): Double {
        require(params is SpeciesParams.Ferret) {
            "FerretAgeCalculator requires SpeciesParams.Ferret, got ${params::class.simpleName}"
        }
        return toHumanYears(ageInYears)
    }

    /**
     * Возвращает возраст хорька в человеческих годах по кусочной формуле §4.7.
     *
     * @param ageInYears возраст хорька в годах (должен быть > 0).
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     */
    fun toHumanYears(ageInYears: Double): Double {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        return when {
            ageInYears < SIX_MONTHS -> RATE_PER_YEAR_BEFORE_SIX_MONTHS * ageInYears
            ageInYears < ONE_YEAR ->
                HUMAN_YEARS_AT_SIX_MONTHS + RATE_PER_YEAR_SIX_TO_TWELVE_MONTHS * (ageInYears - SIX_MONTHS)
            else -> HUMAN_YEARS_AT_ONE_YEAR + RATE_PER_YEAR_AFTER_ONE_YEAR * (ageInYears - ONE_YEAR)
        }
    }

    /** Граница первого сегмента — 6 месяцев (0.5 года). */
    private const val SIX_MONTHS: Double = 0.5

    /** Граница второго сегмента — 1 год. */
    private const val ONE_YEAR: Double = 1.0

    /** Темп первого сегмента: +5 ЧГ/мес. × 12 мес. = 60 ЧГ/год до 6 мес. */
    private const val RATE_PER_YEAR_BEFORE_SIX_MONTHS: Double = 60.0

    /** ЧГ в опорной точке 6 мес. */
    private const val HUMAN_YEARS_AT_SIX_MONTHS: Double = 30.0

    /** Темп второго сегмента: (40 − 30) ЧГ за 0.5 года = 20 ЧГ/год (6–12 мес.). */
    private const val RATE_PER_YEAR_SIX_TO_TWELVE_MONTHS: Double = 20.0

    /** ЧГ в опорной точке 1 год. */
    private const val HUMAN_YEARS_AT_ONE_YEAR: Double = 40.0

    /** Темп третьего сегмента: +4 ЧГ/год после 1 года. */
    private const val RATE_PER_YEAR_AFTER_ONE_YEAR: Double = 4.0
}
