@file:OptIn(io.kotest.common.ExperimentalKotest::class)

package app.pawclock.calculator

import app.pawclock.model.RabbitSize
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.double
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Property-based проверки [RabbitAgeCalculator] (House Rabbit Society + AVMA, §4.3; см. §11.6).
 *
 * Свойства:
 *  1. **Monotonicity:** для любого [RabbitSize] функция `age → humanYears` строго возрастает
 *     (размер на формулу не влияет — достаточно одного, но проверяем все для регрессий API).
 *  2. **Bounded jump (вместо continuity):** формула §4.3 **намеренно разрывна** на стыках
 *     `age = 0.33` и `age = 1.0` (привязана к опорным точкам 12 и 21 ЧГ), поэтому строгую
 *     непрерывность заявить нельзя. Вместо неё проверяем, что скачок на каждом стыке
 *     положителен (монотонность сохраняется) и ограничен сверху [BOUNDED_JUMP] ЧГ.
 *  3. **Positivity:** результат > 0 для любого `age > 0`.
 *  4. **Boundedness:** для `age ≤ 30` (выше любой реальной ЧЖ кролика) результат < 200 ЧГ.
 *
 * Эти инварианты ловят регрессии, которые табличные тесты пропускают:
 *  - случайный знак минус в коэффициенте сегмента,
 *  - раздувшийся скачок на стыке (например, перепутанная опорная точка 21 → 210),
 *  - переполнение при больших age.
 *
 * Источники: House Rabbit Society *Rabbit Life Stages*; AVMA guidelines. См. §4.3 и ADR-0006.
 */
class RabbitAgeCalculatorPropertyTest {
    private val calculator = RabbitAgeCalculator

    private val config = PropTestConfig(iterations = PROPERTY_ITERATIONS)

    @Test
    fun `rabbit age is monotonically non-decreasing for every size`() =
        runBlocking {
            RabbitSize.entries.forEach { size ->
                checkAll(
                    config,
                    Arb.double(MIN_AGE, MAX_REASONABLE_AGE),
                    Arb.double(MIN_AGE, MAX_REASONABLE_AGE),
                ) { a, b ->
                    val (lo, hi) = if (a <= b) a to b else b to a
                    val loYears = calculator.toHumanYears(lo, size)
                    val hiYears = calculator.toHumanYears(hi, size)
                    assertTrue(
                        loYears <= hiYears,
                        "$size: monotonicity violated f($lo)=$loYears > f($hi)=$hiYears",
                    )
                }
            }
        }

    @Test
    fun `rabbit jump at piecewise boundaries is positive and bounded`() =
        runBlocking {
            val boundaries = listOf(RabbitAgeCalculator.FOUR_MONTHS, RabbitAgeCalculator.ONE_YEAR)
            checkAll(config, Arb.double(SMALL_EPS_MIN, SMALL_EPS_MAX)) { eps ->
                boundaries.forEach { boundary ->
                    val left = calculator.toHumanYears(boundary - eps, RabbitSize.Medium)
                    val right = calculator.toHumanYears(boundary + eps, RabbitSize.Medium)
                    val jump = right - left
                    assertTrue(
                        jump in 0.0..BOUNDED_JUMP,
                        "Jump at $boundary (eps=$eps) = $jump out of [0, $BOUNDED_JUMP]",
                    )
                }
            }
        }

    @Test
    fun `rabbit age is strictly positive for positive input`() =
        runBlocking {
            checkAll(config, Arb.double(MIN_AGE, MAX_REASONABLE_AGE)) { age ->
                val result = calculator.toHumanYears(age, RabbitSize.Medium)
                assertTrue(result > 0.0, "Expected positive result for age=$age, got $result")
            }
        }

    @Test
    fun `rabbit age is bounded above by 200 human years up to 30 years`() =
        runBlocking {
            // f(30) = 21 + 6·29 = 195 < 200. Граница ловит регрессии с неверной опорной точкой/наклоном.
            checkAll(config, Arb.double(MIN_AGE, MAX_REASONABLE_AGE)) { age ->
                val result = calculator.toHumanYears(age, RabbitSize.Medium)
                assertTrue(result < UPPER_BOUND_HUMAN_YEARS, "Age $age → $result ≥ 200 ЧГ")
            }
        }

    @Test
    fun `rabbit throws on zero or negative ages`() =
        runBlocking {
            checkAll(config, Arb.double(-MAX_REASONABLE_AGE, 0.0)) { age ->
                val result = runCatching { calculator.toHumanYears(age, RabbitSize.Medium) }
                assertTrue(
                    result.isFailure && result.exceptionOrNull() is IllegalArgumentException,
                    "Expected IllegalArgumentException for age=$age, got $result",
                )
            }
        }

    private companion object {
        private const val PROPERTY_ITERATIONS = 300
        private const val MIN_AGE = 0.01
        private const val MAX_REASONABLE_AGE = 30.0
        private const val UPPER_BOUND_HUMAN_YEARS = 200.0

        // Окно ε для стыков: [1e-6, 1e-4] — достаточно узкое, чтобы остаться в соседних сегментах.
        private const val SMALL_EPS_MIN = 1.0e-6
        private const val SMALL_EPS_MAX = 1.0e-4

        // Фактические скачки §4.3: ≈ +2.1 ЧГ на 0.33 и ≈ +3.64 ЧГ на 1.0; запас до 4.5.
        private const val BOUNDED_JUMP = 4.5
    }
}
