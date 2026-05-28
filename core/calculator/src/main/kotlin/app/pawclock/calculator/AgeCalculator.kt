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
         * Возвращает калькулятор для заданного вида.
         *
         * После Plan 2 Task 10 реализованы все 12 видов, поэтому `when` ниже исчерпывающий и
         * фактически всегда возвращает не-null. Nullable-тип контракта сохранён намеренно:
         * вызывающий слой (`CalculatePetAgeUseCase`) исторически реагировал на `null` бросанием
         * `UnsupportedSpeciesException`, и эта защитная ветка остаётся на случай добавления
         * нового вида-stub в будущем.
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
                Species.Ferret -> FerretAgeCalculator
                Species.Bird -> BirdAgeCalculator
                Species.Reptile -> ReptileAgeCalculator
                Species.Horse -> HorseAgeCalculator
                Species.Fish -> FishAgeCalculator
            }
    }
}
