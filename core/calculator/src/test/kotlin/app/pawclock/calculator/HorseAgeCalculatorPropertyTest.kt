@file:OptIn(io.kotest.common.ExperimentalKotest::class)

package app.pawclock.calculator

import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.double
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Property-based проверки [HorseAgeCalculator] (AAEP + PetMD, §4.10; см. §11.6).
 *
 * 3-фазная кусочно-линейная формула (тип лошади на формулу не влияет):
 *  1. **Monotonicity:** функция строго возрастает (наклоны 6.5 → 6.5 → 5 → 2.5, все > 0).
 *  2. **Continuity:** формула §4.10 **непрерывна** на стыках 1, 2, 3 года (опорные точки
 *     6.5 / 13 / 18 ЧГ). Сегменты «4-й год» и «>4 лет» имеют равный наклон → точка 4 г.
 *     лежит внутри одного сегмента, разрыва там также нет. Проверяем все четыре.
 *  3. **Positivity:** результат > 0 для любого `age > 0`.
 *
 * Источники: AAEP Vaccination / Senior Horse Care; Schraer K. (DVM), PetMD. См. §4.10 и ADR-0006.
 */
class HorseAgeCalculatorPropertyTest {
    private val calculator = HorseAgeCalculator

    private val config = PropTestConfig(iterations = PROPERTY_ITERATIONS)

    @Test
    fun `horse age is monotonically non-decreasing`() =
        runBlocking {
            checkAll(
                config,
                Arb.double(MIN_AGE, MAX_REASONABLE_AGE),
                Arb.double(MIN_AGE, MAX_REASONABLE_AGE),
            ) { a, b ->
                val (lo, hi) = if (a <= b) a to b else b to a
                assertTrue(
                    calculator.toHumanYears(lo) <= calculator.toHumanYears(hi),
                    "Monotonicity violated f($lo) > f($hi)",
                )
            }
        }

    @Test
    fun `horse formula is continuous at phase boundaries`() =
        runBlocking {
            val boundaries = listOf(ONE_YEAR, TWO_YEARS, THREE_YEARS, FOUR_YEARS)
            checkAll(config, Arb.double(SMALL_EPS_MIN, SMALL_EPS_MAX)) { eps ->
                boundaries.forEach { boundary ->
                    val left = calculator.toHumanYears(boundary - eps)
                    val right = calculator.toHumanYears(boundary + eps)
                    assertTrue(
                        kotlin.math.abs(right - left) < CONTINUITY_TOLERANCE,
                        "Discontinuity at $boundary (eps=$eps): left=$left right=$right",
                    )
                }
            }
        }

    @Test
    fun `horse age is strictly positive for positive input`() =
        runBlocking {
            checkAll(config, Arb.double(MIN_AGE, MAX_REASONABLE_AGE)) { age ->
                val result = calculator.toHumanYears(age)
                assertTrue(result > 0.0, "Expected positive result for age=$age, got $result")
            }
        }

    @Test
    fun `horse throws on zero or negative ages`() =
        runBlocking {
            checkAll(config, Arb.double(-MAX_REASONABLE_AGE, 0.0)) { age ->
                val result = runCatching { calculator.toHumanYears(age) }
                assertTrue(
                    result.isFailure && result.exceptionOrNull() is IllegalArgumentException,
                    "Expected IllegalArgumentException for age=$age, got $result",
                )
            }
        }

    private companion object {
        private const val PROPERTY_ITERATIONS = 300
        private const val MIN_AGE = 0.01

        // Максимальный реальный возраст лошади ≈ 40 лет (с запасом).
        private const val MAX_REASONABLE_AGE = 40.0

        // Стыки фаз §4.10 (константы калькулятора private — дублируем как литералы здесь).
        private const val ONE_YEAR = 1.0
        private const val TWO_YEARS = 2.0
        private const val THREE_YEARS = 3.0
        private const val FOUR_YEARS = 4.0

        private const val SMALL_EPS_MIN = 1.0e-6
        private const val SMALL_EPS_MAX = 1.0e-4

        // При ε ≤ 1e-4 и наклоне ≤ 6.5 ЧГ/год скачок < 0.0013; запас 0.05.
        private const val CONTINUITY_TOLERANCE = 0.05
    }
}
