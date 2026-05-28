package app.pawclock.calculator

import app.pawclock.model.CalculationMethod
import app.pawclock.model.CatType
import app.pawclock.model.DogSize

/**
 * Параметры расчёта возраста/стадии жизни, специфичные для каждого вида.
 *
 * Sealed-маркер, передаваемый в унифицированные методы [AgeCalculator.toHumanYears] и
 * [LifeStageCalculator.determine]. Каждая реализация [AgeCalculator] ожидает «свой»
 * подтип и проверяет это через `require(params is …)` — это сознательный компромисс
 * type-safety ради того, чтобы [AgeCalculator.forSpecies] возвращал единый тип, который
 * `CalculatePetAgeUseCase` вызывает без generics.
 *
 * Расширяется по мере добавления видов в Plan 2 (Rabbit, Hamster, … — каждый со своим
 * набором подкатегорий). См. спецификацию PawClock §4.
 */
sealed interface SpeciesParams {
    /**
     * Параметры собаки.
     *
     * @param method выбранный метод расчёта (Wang EPIGENETIC / AKC SIZE_BASED).
     * @param size размер собаки (для SIZE_BASED обязателен; для EPIGENETIC игнорируется,
     *   но передаётся для расчёта стадии жизни через [DogLifeStageCalculator]).
     */
    data class Dog(
        val method: CalculationMethod,
        val size: DogSize,
    ) : SpeciesParams

    /**
     * Параметры кошки.
     *
     * @param type тип содержания (влияет на поправки старения и порог EndOfLife).
     */
    data class Cat(
        val type: CatType,
    ) : SpeciesParams
}
