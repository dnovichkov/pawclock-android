package app.pawclock.calculator

import kotlin.math.ln
import kotlin.math.pow

/**
 * Калькулятор возраста собаки в человеческих годах.
 *
 * Поддерживаемые методы расчёта:
 *  - [CalculationMethod.EPIGENETIC] — формула Wang T. et al. (Cell Systems 2020).
 *  - [CalculationMethod.SIZE_BASED] — табличный метод AKC/AAHA 2019
 *    (реализуется в Task 7, требует параметра `DogSize`).
 *
 * Эпигенетическая формула:
 * ```
 * ЧГ = 16 · ln(возраст_в_годах) + 31      (при возраст ≥ 1 год)
 * ```
 *
 * Для возраста < 1 года Wang-формула математически невалидна (даёт отрицательные значения
 * при `age < e^(-31/16) ≈ 0.144`). Поэтому для puppy-стадии применяется кусочное
 * расширение — степенная интерполяция `ЧГ = 31 · age^0.6`. Эта функция:
 *  - непрерывна с Wang-формулой в точке `age = 1.0` (обе ветви дают 31 ЧГ);
 *  - даёт `0 ЧГ` при `age = 0.0`;
 *  - монотонно возрастает;
 *  - соответствует биологически быстрому старению щенков в первый год жизни.
 *
 * Источник для [CalculationMethod.EPIGENETIC]:
 * Wang T., Tsui B., Kreisberg J.F. et al.
 * "Quantitative Translation of Dog-to-Human Aging by Conserved Remodeling
 *  of the DNA Methylome". Cell Systems, 2020; 11(2):176-185.
 * DOI: 10.1016/j.cels.2020.06.006
 *
 * См. также спецификацию PawClock §4.1 и ADR-0006.
 */
class DogAgeCalculator {
    /**
     * Возвращает возраст собаки в человеческих годах.
     *
     * @param ageInYears возраст собаки в годах (должен быть > 0).
     * @param method метод расчёта.
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     */
    fun toHumanYears(
        ageInYears: Double,
        method: CalculationMethod,
    ): Double {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        return when (method) {
            CalculationMethod.EPIGENETIC -> epigeneticHumanYears(ageInYears)
            CalculationMethod.SIZE_BASED ->
                error("SIZE_BASED requires DogSize parameter — implemented in Task 7")
        }
    }

    private fun epigeneticHumanYears(ageInYears: Double): Double =
        if (ageInYears >= 1.0) {
            WANG_COEFFICIENT * ln(ageInYears) + WANG_OFFSET
        } else {
            // Puppy-расширение: степенная интерполяция, непрерывная с Wang в age=1.
            WANG_OFFSET * ageInYears.pow(WANG_PUPPY_EXPONENT)
        }

    internal companion object {
        /** Коэффициент при `ln(age)` в формуле Wang 2020. */
        internal const val WANG_COEFFICIENT: Double = 16.0

        /** Сдвиг (значение ЧГ при `age = 1`) в формуле Wang 2020. */
        internal const val WANG_OFFSET: Double = 31.0

        /**
         * Экспонента в степенной аппроксимации для puppy-стадии.
         *
         * Выбрана эмпирически: даёт ~9 ЧГ для 7-недельного щенка и ~20 ЧГ для 6-месячного,
         * что согласуется с AAHA puppy-guidance и обеспечивает непрерывность в age=1.
         */
        internal const val WANG_PUPPY_EXPONENT: Double = 0.6
    }
}
