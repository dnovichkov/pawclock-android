package app.pawclock.calculator

/**
 * Метод расчёта возраста животного в человеческих годах.
 *
 * См. ADR-0006 (Wang et al. формула как default для собак) и спецификацию PawClock §4.1.
 *
 * @see DogAgeCalculator
 */
enum class CalculationMethod {
    /**
     * Эпигенетическая формула.
     *
     * Для собак: Wang T. et al., Cell Systems 2020 — `ЧГ = 16 · ln(age) + 31`
     * (DOI: 10.1016/j.cels.2020.06.006).
     */
    EPIGENETIC,

    /**
     * Табличный размерный метод.
     *
     * Для собак: AKC/AAHA 2019 — табличные значения, зависящие от размерного класса
     * (Toy/Small/Medium/Large/Giant). Реализуется в Task 7.
     */
    SIZE_BASED,
}
