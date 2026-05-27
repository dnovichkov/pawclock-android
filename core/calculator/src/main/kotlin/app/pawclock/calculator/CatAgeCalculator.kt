package app.pawclock.calculator

import app.pawclock.model.CatType

/**
 * Калькулятор возраста кошки в человеческих годах по руководящим
 * принципам AAHA / AAFP 2021 (см. §4.2 спецификации PawClock).
 *
 * Базовая кусочная формула (без поправок):
 * ```
 * age ≤ 1                → 15 · age                      // линейная интерполяция к 15 ЧГ
 * 1 < age ≤ 2            → 15 + 9 · (age − 1)            // плавный переход к 24 ЧГ к 2-м годам
 * age > 2                → 24 + 4 · (age − 2)            // далее +4 ЧГ/год
 * ```
 *
 * Поправки (применяются только при `age > 2`):
 *  - [CatType.Outdoor] → результат × 1.15 (приближённая поправка
 *    на быстрое старение уличных кошек по AAFP 2021).
 *  - [CatType.LargeBreed] → результат + (age − 2) (для Мейн-Кунов
 *    и других крупных пород, +1 ЧГ/год после 2 лет).
 *
 * Для [CatType.IndoorShortHair] и [CatType.IndoorLongHair] поправок нет
 * (длина шерсти не влияет на скорость старения per AAFP).
 *
 * Источник:
 * Quimby J., Gowland S., Carney H.C., DePorter T., Plummer P., Westropp J.
 * "2021 AAHA / AAFP Feline Life Stage Guidelines".
 * Journal of Feline Medicine and Surgery, 2021; 23(3):211-233.
 * DOI: 10.1177/1098612X21993657
 *
 * См. также спецификацию PawClock §4.2 и ADR-0006.
 */
class CatAgeCalculator {
    /**
     * Возвращает возраст кошки в человеческих годах.
     *
     * @param ageInYears возраст кошки в годах (должен быть > 0).
     * @param catType тип содержания, влияющий на поправки (см. KDoc класса).
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     */
    fun toHumanYears(
        ageInYears: Double,
        catType: CatType,
    ): Double {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        val baseline = baselineHumanYears(ageInYears)
        return applyCorrections(baseline, ageInYears, catType)
    }

    private fun baselineHumanYears(ageInYears: Double): Double =
        when {
            ageInYears <= FIRST_YEAR_THRESHOLD ->
                FIRST_YEAR_HUMAN_AGE * ageInYears
            ageInYears <= SECOND_YEAR_THRESHOLD ->
                FIRST_YEAR_HUMAN_AGE +
                    SECOND_YEAR_INCREMENT * (ageInYears - FIRST_YEAR_THRESHOLD)
            else ->
                SECOND_YEAR_HUMAN_AGE +
                    SUBSEQUENT_YEAR_INCREMENT * (ageInYears - SECOND_YEAR_THRESHOLD)
        }

    private fun applyCorrections(
        baseline: Double,
        ageInYears: Double,
        catType: CatType,
    ): Double {
        if (ageInYears <= SECOND_YEAR_THRESHOLD) return baseline
        var result = baseline
        if (catType.isOutdoor) {
            result *= OUTDOOR_AGING_FACTOR
        }
        if (catType.isLargeBreed) {
            result += ageInYears - SECOND_YEAR_THRESHOLD
        }
        return result
    }

    internal companion object {
        /** Граница "котёнок до года": age ≤ 1 — линейная интерполяция к [FIRST_YEAR_HUMAN_AGE]. */
        internal const val FIRST_YEAR_THRESHOLD: Double = 1.0

        /** Граница «второго года»: 1 < age ≤ 2 — линейная интерполяция от 15 ЧГ к [SECOND_YEAR_HUMAN_AGE]. */
        internal const val SECOND_YEAR_THRESHOLD: Double = 2.0

        /** Человеческий возраст в 1 год по AAFP 2021. */
        internal const val FIRST_YEAR_HUMAN_AGE: Double = 15.0

        /** Человеческий возраст в 2 года по AAFP 2021. */
        internal const val SECOND_YEAR_HUMAN_AGE: Double = 24.0

        /** Прирост ЧГ за 2-й год жизни: 24 − 15 = 9. */
        internal const val SECOND_YEAR_INCREMENT: Double = 9.0

        /** Прирост ЧГ за каждый год после 2-го по AAFP 2021. */
        internal const val SUBSEQUENT_YEAR_INCREMENT: Double = 4.0

        /**
         * Множитель ускоренного старения для уличных кошек.
         * AAFP 2021 рекомендует приближённую поправку × 1.15 после 2 лет.
         */
        internal const val OUTDOOR_AGING_FACTOR: Double = 1.15
    }
}
