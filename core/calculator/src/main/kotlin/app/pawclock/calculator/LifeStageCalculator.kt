package app.pawclock.calculator

import app.pawclock.model.LifeStage
import app.pawclock.model.Species

/**
 * Стратегия определения стадии жизни питомца (Strategy pattern, парный к [AgeCalculator]).
 *
 * Каждый вид имеет ровно одну реализацию-`data object` ([DogLifeStageCalculator],
 * [CatLifeStageCalculator], …). Диспатч — через фабрику [forSpecies].
 *
 * См. спецификацию PawClock §4 и ADR-0006.
 */
sealed interface LifeStageCalculator {
    /** Вид, для которого предназначена данная реализация. */
    val species: Species

    /**
     * Возвращает стадию жизни питомца.
     *
     * @param ageInYears календарный возраст в годах (должен быть > 0).
     * @param params параметры вида; должны соответствовать [species], иначе
     *   бросается [IllegalArgumentException].
     * @throws IllegalArgumentException если `ageInYears <= 0` или тип [params] не совпадает с видом.
     */
    fun determine(
        ageInYears: Double,
        params: SpeciesParams,
    ): LifeStage

    companion object {
        /**
         * Возвращает калькулятор стадии жизни для вида или `null`, если вид ещё не реализован.
         */
        fun forSpecies(species: Species): LifeStageCalculator? =
            when (species) {
                Species.Dog -> DogLifeStageCalculator
                Species.Cat -> CatLifeStageCalculator
                Species.Rabbit -> RabbitLifeStageCalculator
                Species.Hamster -> HamsterLifeStageCalculator
                Species.GuineaPig -> GuineaPigLifeStageCalculator
                Species.Rat -> RatLifeStageCalculator
                Species.Mouse -> MouseLifeStageCalculator
                Species.Ferret -> FerretLifeStageCalculator
                else -> null
            }
    }
}
