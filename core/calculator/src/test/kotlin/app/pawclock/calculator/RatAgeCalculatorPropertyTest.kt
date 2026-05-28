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
 * Property-based проверки [RatAgeCalculator] (Sengupta 2013, §4.6; см. §11.6).
 *
 * Формула линейна (`ЧГ = 13.8·age + 1.4`), поэтому свойства тривиальны, но полезны как страховка
 * от регрессий коэффициентов:
 *  1. **Monotonicity:** строго возрастает (положительный коэффициент 13.8).
 *  2. **Affine invariant:** разность `f(a) − f(b)` равна `13.8·(a − b)` — линейность без изломов.
 *  3. **Positivity:** результат > 0 (свободный член 1.4 > 0 гарантирует это даже у малых age).
 *
 * Источник: Sengupta P. (2013), Int J Prev Med 4(6):624–630, PMC3733029. См. §4.6 и ADR-0006.
 */
class RatAgeCalculatorPropertyTest {
    private val calculator = RatAgeCalculator

    private val config = PropTestConfig(iterations = PROPERTY_ITERATIONS)

    @Test
    fun `rat age is monotonically non-decreasing`() =
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
    fun `rat formula slope equals the Sengupta coefficient everywhere`() =
        runBlocking {
            checkAll(
                config,
                Arb.double(MIN_AGE, MAX_REASONABLE_AGE),
                Arb.double(MIN_AGE, MAX_REASONABLE_AGE),
            ) { a, b ->
                val expectedDelta = RatAgeCalculator.RAT_COEFFICIENT * (a - b)
                val actualDelta = calculator.toHumanYears(a) - calculator.toHumanYears(b)
                assertTrue(
                    kotlin.math.abs(actualDelta - expectedDelta) < SLOPE_TOLERANCE,
                    "Non-affine at a=$a b=$b: expected Δ=$expectedDelta, got $actualDelta",
                )
            }
        }

    @Test
    fun `rat age is strictly positive for positive input`() =
        runBlocking {
            checkAll(config, Arb.double(MIN_AGE, MAX_REASONABLE_AGE)) { age ->
                val result = calculator.toHumanYears(age)
                assertTrue(result > 0.0, "Expected positive result for age=$age, got $result")
            }
        }

    @Test
    fun `rat throws on zero or negative ages`() =
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

        // Максимальный реальный возраст крысы ≈ 4 года (с запасом).
        private const val MAX_REASONABLE_AGE = 4.0

        // Линейность: допускаем только погрешность double-арифметики.
        private const val SLOPE_TOLERANCE = 1.0e-9
    }
}
