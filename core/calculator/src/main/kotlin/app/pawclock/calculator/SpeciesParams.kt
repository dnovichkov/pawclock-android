package app.pawclock.calculator

import app.pawclock.model.CalculationMethod
import app.pawclock.model.CatType
import app.pawclock.model.DogSize
import app.pawclock.model.HamsterType
import app.pawclock.model.RabbitSize

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

    /**
     * Параметры кролика.
     *
     * @param size порода/размер кролика. На формулу расчёта возраста не влияет
     *   (она едина для всех пород — House Rabbit Society), используется только для оценки
     *   ожидаемой продолжительности жизни. См. [RabbitSize] и §4.3 спецификации.
     */
    data class Rabbit(
        val size: RabbitSize,
    ) : SpeciesParams

    /**
     * Параметры хомяка.
     *
     * @param type вид хомяка. На формулу расчёта возраста не влияет (она едина для всех
     *   видов — RVC VetCompass), используется только для оценки ожидаемой продолжительности
     *   жизни. См. [HamsterType] и §4.4 спецификации.
     */
    data class Hamster(
        val type: HamsterType,
    ) : SpeciesParams

    /**
     * Параметры морской свинки.
     *
     * У морской свинки нет подкатегорий (формула §4.5 едина для всех, размер на расчёт
     * не влияет), поэтому это `data object` без полей — маркер, передаваемый в
     * [GuineaPigAgeCalculator] / [GuineaPigLifeStageCalculator]. См. §4.5 спецификации.
     */
    data object GuineaPig : SpeciesParams

    /**
     * Параметры крысы.
     *
     * У крысы нет подкатегорий (линейная формула §4.6 Sengupta 2013 едина для всех),
     * поэтому это `data object` без полей — маркер, передаваемый в
     * [RatAgeCalculator] / [RatLifeStageCalculator]. См. §4.6 спецификации.
     */
    data object Rat : SpeciesParams

    /**
     * Параметры мыши.
     *
     * У мыши нет подкатегорий (кусочная формула §4.6 Dutta & Sengupta 2016 едина для всех),
     * поэтому это `data object` без полей — маркер, передаваемый в
     * [MouseAgeCalculator] / [MouseLifeStageCalculator]. См. §4.6 спецификации.
     */
    data object Mouse : SpeciesParams

    /**
     * Параметры хорька.
     *
     * У хорька нет подкатегорий (кусочная формула §4.7 едина для всех), поэтому это
     * `data object` без полей — маркер, передаваемый в [FerretAgeCalculator] /
     * [FerretLifeStageCalculator]. См. §4.7 спецификации.
     */
    data object Ferret : SpeciesParams
}
