// Числовые литералы здесь — табличные значения published-стандарта AKC/AAHA 2019.
// Они не "magic": каждое имеет физический смысл (возраст собаки в человеческих годах),
// все собраны в KDoc-таблице ниже с указанием первоисточника, и любая отдельная константа
// (например, `WEEK_3_HUMAN_AGE = 28`) только ухудшила бы читаемость.
@file:Suppress("detekt:MagicNumber")

package app.pawclock.calculator

import app.pawclock.model.DogSize

/**
 * Табличные значения возраста собаки в человеческих годах по AKC / AAHA 2019.
 *
 * Источник: American Kennel Club + American Animal Hospital Association,
 * 2019 AAHA Canine Life Stage Guidelines.
 * См. также спецификацию PawClock §4.1 и ADR-0006.
 *
 * Таблица из §4.1:
 *
 * | Возраст | Малая ≤9 кг | Средняя 9–23 | Крупная 23–45 | Гигантская >45 |
 * |---------|-------------|--------------|---------------|----------------|
 * |   1     |     15      |      15      |      15       |       12       |
 * |   2     |     24      |      24      |      24       |       22       |
 * |   3     |     28      |      28      |      28       |       31       |
 * |   4     |     32      |      32      |      32       |       38       |
 * |   5     |     36      |      36      |      36       |       45       |
 * |   6     |     40      |      42      |      45       |       49       |
 * |   7     |     44      |      47      |      50       |       56       |
 * |   8     |     48      |      51      |      55       |       64       |
 * |  10     |     56      |      60      |      66       |       79       |
 * |  12     |     64      |      69      |      77       |       93       |
 * |  14     |     72      |      78      |      88       |      107       |
 * |  15     |     76      |      83      |      93       |      114       |
 *
 * В типе [DogSize] подгруппы [DogSize.Toy] и [DogSize.Small] обе мапятся на колонку
 * «Малая ≤9 кг»: AKC/AAHA 2019 не разделяет миниатюрных и малых собак для целей
 * расчёта стареющего возраста, граница идёт по 9 кг.
 *
 * Между табличными точками применяется линейная интерполяция.
 * За пределами таблицы (`age > 15`) применяется линейная экстраполяция с использованием
 * наклона между последними двумя точками (14 и 15 лет).
 * Для `age < 1` применяется линейная интерполяция от нуля к первой табличной точке.
 */
internal object DogSizeTable {
    /** Опорные возрасты в годах (общие для всех колонок). */
    internal val ANCHOR_AGES: DoubleArray =
        doubleArrayOf(
            1.0,
            2.0,
            3.0,
            4.0,
            5.0,
            6.0,
            7.0,
            8.0,
            10.0,
            12.0,
            14.0,
            15.0,
        )

    /** Колонка «Малая ≤9 кг» — используется для Toy и Small. */
    private val SMALL_HUMAN_YEARS: DoubleArray =
        doubleArrayOf(15.0, 24.0, 28.0, 32.0, 36.0, 40.0, 44.0, 48.0, 56.0, 64.0, 72.0, 76.0)

    /** Колонка «Средняя 9–23 кг». */
    private val MEDIUM_HUMAN_YEARS: DoubleArray =
        doubleArrayOf(15.0, 24.0, 28.0, 32.0, 36.0, 42.0, 47.0, 51.0, 60.0, 69.0, 78.0, 83.0)

    /** Колонка «Крупная 23–45 кг». */
    private val LARGE_HUMAN_YEARS: DoubleArray =
        doubleArrayOf(15.0, 24.0, 28.0, 32.0, 36.0, 45.0, 50.0, 55.0, 66.0, 77.0, 88.0, 93.0)

    /** Колонка «Гигантская >45 кг». */
    private val GIANT_HUMAN_YEARS: DoubleArray =
        doubleArrayOf(12.0, 22.0, 31.0, 38.0, 45.0, 49.0, 56.0, 64.0, 79.0, 93.0, 107.0, 114.0)

    /**
     * Возвращает значение ЧГ из таблицы для заданного [size] и [ageInYears] с применением
     * линейной интерполяции/экстраполяции по описанным выше правилам.
     */
    internal fun humanYears(
        size: DogSize,
        ageInYears: Double,
    ): Double {
        val column = columnFor(size)
        val anchors = ANCHOR_AGES
        val firstAnchor = anchors[0]
        val lastIndex = anchors.lastIndex
        val lastAnchor = anchors[lastIndex]
        return when {
            // Случай 1: возраст меньше первой опорной точки → интерполяция от 0 к первой точке.
            ageInYears < firstAnchor -> column[0] * (ageInYears / firstAnchor)
            // Случай 2: возраст за пределами таблицы → экстраполяция наклоном последних точек.
            ageInYears > lastAnchor -> {
                val slope =
                    (column[lastIndex] - column[lastIndex - 1]) /
                        (lastAnchor - anchors[lastIndex - 1])
                column[lastIndex] + slope * (ageInYears - lastAnchor)
            }
            // Случай 3: внутри таблицы → линейная интерполяция между соседними точками.
            else -> interpolateWithinTable(anchors, column, ageInYears)
        }
    }

    private fun interpolateWithinTable(
        anchors: DoubleArray,
        column: DoubleArray,
        ageInYears: Double,
    ): Double {
        for (i in 0 until anchors.lastIndex) {
            val lo = anchors[i]
            val hi = anchors[i + 1]
            if (ageInYears in lo..hi) {
                val ratio = (ageInYears - lo) / (hi - lo)
                return column[i] + ratio * (column[i + 1] - column[i])
            }
        }
        // Недостижимо: caller гарантирует ageInYears ∈ [anchors[0]; anchors[last]].
        error("Unreachable: ageInYears=$ageInYears not bracketed by anchor ages")
    }

    private fun columnFor(size: DogSize): DoubleArray =
        when (size) {
            DogSize.Toy, DogSize.Small -> SMALL_HUMAN_YEARS
            DogSize.Medium -> MEDIUM_HUMAN_YEARS
            DogSize.Large -> LARGE_HUMAN_YEARS
            DogSize.Giant -> GIANT_HUMAN_YEARS
        }
}
