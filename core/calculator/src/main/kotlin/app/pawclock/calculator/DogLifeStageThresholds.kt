// Числовые литералы здесь — табличные пороги начала Senior-стадии по AAHA 2019.
// Каждое значение имеет ветеринарный смысл (возраст в годах) и собрано в KDoc.
@file:Suppress("detekt:MagicNumber")

package app.pawclock.calculator

import app.pawclock.model.DogSize

/**
 * Пороги переходов между стадиями жизни собаки по размеру (AAHA 2019, §4.1 спецификации).
 *
 * | Размер     | Senior с | EndOfLife с | Зазор Senior |
 * |------------|----------|-------------|--------------|
 * | Toy        |    11    |     15      |     4 года   |
 * | Small      |    11    |     15      |     4 года   |
 * | Medium     |     9    |     11      |     2 года   |
 * | Large      |     7    |     10      |     3 года   |
 * | Giant      |     5    |      7      |     2 года   |
 *
 * Зазор между Senior и EndOfLife для крупных пород короче — согласно McMillan 2024,
 * крупные собаки имеют более быстрое старение после старта Senior-стадии. Поэтому единая
 * формула типа `endOfLife = lifespanUpperBound - 3` приводила бы к коллапсу Senior-окна
 * для Medium/Large/Giant — мы используем табличные значения.
 *
 * Источники:
 *  - 2019 AAHA Canine Life Stage Guidelines (senior thresholds).
 *  - McMillan K.M. et al. Scientific Reports, 2024 (lifespan + decline rates).
 */
internal object DogLifeStageThresholds {
    internal fun seniorStart(size: DogSize): Double =
        when (size) {
            DogSize.Toy, DogSize.Small -> 11.0
            DogSize.Medium -> 9.0
            DogSize.Large -> 7.0
            DogSize.Giant -> 5.0
        }

    internal fun endOfLifeStart(size: DogSize): Double =
        when (size) {
            DogSize.Toy, DogSize.Small -> 15.0
            DogSize.Medium -> 11.0
            DogSize.Large -> 10.0
            DogSize.Giant -> 7.0
        }
}
