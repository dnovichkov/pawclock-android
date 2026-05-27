// Числовые литералы здесь — табличные пороги стадий жизни собак из AAHA 2019.
// Каждое значение имеет ветеринарный смысл (возраст в годах перехода между стадиями),
// все собраны в KDoc-таблице ниже с указанием первоисточника.
@file:Suppress("detekt:MagicNumber")

package app.pawclock.calculator

import app.pawclock.model.DogSize
import app.pawclock.model.LifeStage

/**
 * Калькулятор стадии жизни собаки по возрасту и размеру.
 *
 * Использует пороги AAHA 2019 Canine Life Stage Guidelines (§4.1 спецификации):
 *
 * | Стадия       | Возраст начала                                                          |
 * |--------------|-------------------------------------------------------------------------|
 * | Puppy        | 0 — половая зрелость (~9 мес = 0.75 г, единый порог для всех размеров)  |
 * | YoungAdult   | от половой зрелости до 3 лет (социальная зрелость)                      |
 * | MatureAdult  | 3 года — начало senior (зависит от размера)                             |
 * | Senior       | Toy/Small: 11+, Medium: 9+, Large: 7+, Giant: 5+                        |
 * | EndOfLife    | Toy/Small: 15+, Medium: 11+, Large: 10+, Giant: 7+                      |
 *
 * Пороги EndOfLife — табличные (см. [DogLifeStageThresholds]) и не равны `lifespanUpperBound - N`
 * с единым N: для крупных пород Senior-фаза короче, что отражает более быстрое угасание
 * (McMillan 2024). Единый формульный подход приводил бы к коллапсу Senior-окна.
 *
 * Источники:
 *  - American Animal Hospital Association, 2019 AAHA Canine Life Stage Guidelines.
 *  - McMillan K.M., Bielby J., Williams C.L. et al. "Longevity of companion dog breeds:
 *    those at risk from early death". Scientific Reports, 2024 (n=584 734).
 *
 * См. также спецификацию PawClock §4.1 и ADR-0006.
 */
class DogLifeStageCalculator {
    /**
     * Возвращает стадию жизни собаки по возрасту и размеру.
     *
     * @param ageInYears возраст собаки в годах (должен быть > 0).
     * @param size размер собаки.
     * @throws IllegalArgumentException если `ageInYears <= 0`.
     */
    fun determine(
        ageInYears: Double,
        size: DogSize,
    ): LifeStage.Dog {
        require(ageInYears > 0) { "Age must be positive, got $ageInYears" }
        val endOfLifeStart = DogLifeStageThresholds.endOfLifeStart(size)
        val seniorStart = DogLifeStageThresholds.seniorStart(size)
        return when {
            ageInYears >= endOfLifeStart -> LifeStage.Dog.EndOfLife
            ageInYears >= seniorStart -> LifeStage.Dog.Senior
            ageInYears >= MATURE_ADULT_START -> LifeStage.Dog.MatureAdult
            ageInYears >= YOUNG_ADULT_START -> LifeStage.Dog.YoungAdult
            else -> LifeStage.Dog.Puppy
        }
    }

    /**
     * Возвращает ожидаемый диапазон продолжительности жизни (в годах) для заданного размера.
     *
     * Значения по McMillan et al. 2024 (Scientific Reports, n=584 734):
     *  - Toy/Small: 12–18 лет
     *  - Medium: 10–13 лет
     *  - Large: 8–12 лет
     *  - Giant: 6–8 лет
     *
     * @param size размер собаки.
     */
    fun expectedLifespanRange(size: DogSize): ClosedFloatingPointRange<Double> =
        when (size) {
            DogSize.Toy, DogSize.Small -> 12.0..18.0
            DogSize.Medium -> 10.0..13.0
            DogSize.Large -> 8.0..12.0
            DogSize.Giant -> 6.0..8.0
        }

    internal companion object {
        /**
         * Возраст начала YoungAdult-стадии (≈ 9 месяцев — медиана AAHA-guidelines по
         * половой зрелости: маленькие в 6–9 мес, гигантские в 12–18 мес; используем
         * единый порог 0.75 для всех размеров — упрощение, оправданное для consumer-app).
         */
        internal const val YOUNG_ADULT_START: Double = 0.75

        /**
         * Возраст начала MatureAdult-стадии — социальная зрелость по AAHA 2019.
         */
        internal const val MATURE_ADULT_START: Double = 3.0
    }
}
