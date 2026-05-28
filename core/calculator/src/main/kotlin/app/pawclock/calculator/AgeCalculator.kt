package app.pawclock.calculator

import app.pawclock.model.Species

/**
 * Стратегия расчёта возраста питомца в человеческих годах (Strategy pattern).
 *
 * Каждый поддерживаемый вид имеет ровно одну реализацию-`data object`
 * ([DogAgeCalculator], [CatAgeCalculator], … добавляются в Plan 2). Диспатч по виду
 * выполняется через фабрику [forSpecies], что избавляет `CalculatePetAgeUseCase` от
 * разрастающегося `when (species)` (см. план Plan 2, §"Approaches discussed":
 * Strategy выбран вместо явного when / Registry-map).
 *
 * Унифицированный метод [toHumanYears] принимает [SpeciesParams] — sealed-маркер с
 * параметрами конкретного вида. Реализация проверяет тип через `require(params is …)`.
 *
 * См. спецификацию PawClock §4 и ADR-0006.
 */
sealed interface AgeCalculator {
    /** Вид, для которого предназначена данная реализация. */
    val species: Species

    /**
     * Возвращает возраст в человеческих годах.
     *
     * @param ageInYears календарный возраст в годах (должен быть > 0).
     * @param params параметры вида; должны соответствовать [species], иначе
     *   бросается [IllegalArgumentException].
     * @throws IllegalArgumentException если `ageInYears <= 0` или тип [params] не совпадает с видом.
     */
    fun toHumanYears(
        ageInYears: Double,
        params: SpeciesParams,
    ): Double

    companion object {
        /**
         * Возвращает калькулятор для заданного вида или `null`, если вид ещё не реализован.
         *
         * Контракт `null` (а не исключение) намеренный: вызывающий слой
         * (`CalculatePetAgeUseCase`) сам решает, как реагировать на нереализованный вид
         * (бросает `UnsupportedSpeciesException`).
         */
        fun forSpecies(species: Species): AgeCalculator? =
            when (species) {
                Species.Dog -> DogAgeCalculator
                Species.Cat -> CatAgeCalculator
                Species.Rabbit -> RabbitAgeCalculator
                Species.Hamster -> HamsterAgeCalculator
                Species.GuineaPig -> GuineaPigAgeCalculator
                Species.Rat -> RatAgeCalculator
                Species.Mouse -> MouseAgeCalculator
                else -> null
            }
    }
}
