package app.pawclock.calculator

import app.pawclock.model.DogSize
import kotlin.math.ln
import kotlin.math.pow

/**
 * Калькулятор возраста собаки в человеческих годах.
 *
 * Поддерживаемые методы расчёта:
 *  - [CalculationMethod.EPIGENETIC] — формула Wang T. et al. (Cell Systems 2020),
 *    не требует знания размера собаки.
 *  - [CalculationMethod.SIZE_BASED] — табличный метод AKC / AAHA 2019, требует
 *    параметр [DogSize].
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
 * Табличный метод (SIZE_BASED) использует данные AKC / AAHA 2019 из спецификации §4.1.
 * Между табличными возрастами применяется линейная интерполяция; за пределами таблицы
 * (`age > 15`) — линейная экстраполяция с наклоном последних двух точек.
 * Подробности см. в [DogSizeTable].
 *
 * Источник для [CalculationMethod.EPIGENETIC]:
 * Wang T., Tsui B., Kreisberg J.F. et al.
 * "Quantitative Translation of Dog-to-Human Aging by Conserved Remodeling
 *  of the DNA Methylome". Cell Systems, 2020; 11(2):176-185.
 * DOI: 10.1016/j.cels.2020.06.006
 *
 * Источник для [CalculationMethod.SIZE_BASED]:
 * American Kennel Club + American Animal Hospital Association,
 * 2019 AAHA Canine Life Stage Guidelines.
 *
 * См. также спецификацию PawClock §4.1 и ADR-0006.
 */
class DogAgeCalculator {
    /**
     * Возвращает возраст собаки в человеческих годах по эпигенетической формуле (Wang 2020).
     *
     * Эта перегрузка не требует размера и подходит, когда размер неизвестен либо
     * пользователь явно выбрал EPIGENETIC в настройках.
     *
     * @param ageInYears возраст собаки в годах (должен быть > 0).
     * @param method метод расчёта (см. ограничения ниже).
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     * @throws IllegalStateException если `method == CalculationMethod.SIZE_BASED` —
     *   для табличного метода требуется размер: используйте перегрузку
     *   [toHumanYears] с параметром [DogSize].
     */
    fun toHumanYears(
        ageInYears: Double,
        method: CalculationMethod,
    ): Double {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        return when (method) {
            CalculationMethod.EPIGENETIC -> epigeneticHumanYears(ageInYears)
            CalculationMethod.SIZE_BASED ->
                error(
                    "SIZE_BASED requires a DogSize — call toHumanYears(age, size) " +
                        "or toHumanYears(age, SIZE_BASED, size) instead.",
                )
        }
    }

    /**
     * Возвращает возраст собаки в человеческих годах по табличному методу AKC/AAHA 2019.
     *
     * @param ageInYears возраст собаки в годах (должен быть > 0).
     * @param size размер собаки.
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     */
    fun toHumanYears(
        ageInYears: Double,
        size: DogSize,
    ): Double {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        return DogSizeTable.humanYears(size, ageInYears)
    }

    /**
     * Универсальная перегрузка, выбирающая реализацию по [method]; используется
     * UseCase-слоем, который читает дефолтный метод из настроек.
     *
     * @param ageInYears возраст в годах (должен быть > 0).
     * @param method метод расчёта.
     * @param size размер собаки. Обязателен для [CalculationMethod.SIZE_BASED];
     *   при [CalculationMethod.EPIGENETIC] игнорируется (можно передавать `null`).
     * @throws IllegalArgumentException если `ageInYears <= 0` либо если `size == null`
     *   при выбранном `SIZE_BASED`.
     */
    fun toHumanYears(
        ageInYears: Double,
        method: CalculationMethod,
        size: DogSize?,
    ): Double {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        return when (method) {
            CalculationMethod.EPIGENETIC -> epigeneticHumanYears(ageInYears)
            CalculationMethod.SIZE_BASED -> {
                requireNotNull(size) { "SIZE_BASED method requires DogSize, got null" }
                DogSizeTable.humanYears(size, ageInYears)
            }
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
