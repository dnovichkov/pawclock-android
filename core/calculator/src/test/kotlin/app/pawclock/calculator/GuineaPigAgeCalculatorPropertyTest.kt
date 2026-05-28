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
 * Property-based проверки [GuineaPigAgeCalculator] (Oxbow + Animallama, §4.5; см. §11.6).
 *
 * У морской свинки нет подкатегорий — формула едина, поэтому свойства проверяются на единственной
 * функции `age → humanYears`:
 *  1. **Monotonicity:** функция строго возрастает (5 непрерывных сегментов с положительными наклонами).
 *  2. **Continuity:** формула §4.5 **непрерывна** на всех стыках (0.058, 0.208, 0.42, 4.0) —
 *     каждый сегмент стартует ровно со значения предыдущего. Проверяем отсутствие скачка.
 *  3. **Positivity:** результат > 0 для любого `age > 0`.
 *
 * Источники: Oxbow Animal Health; Animallama. См. §4.5 и ADR-0006.
 */
class GuineaPigAgeCalculatorPropertyTest {
    private val calculator = GuineaPigAgeCalculator

    private val config = PropTestConfig(iterations = PROPERTY_ITERATIONS)

    @Test
    fun `guinea pig age is monotonically non-decreasing`() =
        runBlocking {
            checkAll(
                config,
                Arb.double(MIN_AGE, MAX_REASONABLE_AGE),
                Arb.double(MIN_AGE, MAX_REASONABLE_AGE),
            ) { a, b ->
                val (lo, hi) = if (a <= b) a to b else b to a
                val loYears = calculator.toHumanYears(lo)
                val hiYears = calculator.toHumanYears(hi)
                assertTrue(
                    loYears <= hiYears,
                    "Monotonicity violated f($lo)=$loYears > f($hi)=$hiYears",
                )
            }
        }

    @Test
    fun `guinea pig formula is continuous at piecewise boundaries`() =
        runBlocking {
            val boundaries =
                listOf(
                    GuineaPigAgeCalculator.WEANING_AGE,
                    GuineaPigAgeCalculator.PUBERTY_AGE,
                    GuineaPigAgeCalculator.MATURITY_AGE,
                    GuineaPigAgeCalculator.SENIOR_AGE,
                )
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
    fun `guinea pig age is strictly positive for positive input`() =
        runBlocking {
            checkAll(config, Arb.double(MIN_AGE, MAX_REASONABLE_AGE)) { age ->
                val result = calculator.toHumanYears(age)
                assertTrue(result > 0.0, "Expected positive result for age=$age, got $result")
            }
        }

    @Test
    fun `guinea pig throws on zero or negative ages`() =
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

        // Максимальный реальный возраст морской свинки ≈ 8 лет (с запасом).
        private const val MAX_REASONABLE_AGE = 8.0

        private const val SMALL_EPS_MIN = 1.0e-6
        private const val SMALL_EPS_MAX = 1.0e-4

        // Самый крутой сегмент (0–0.058, наклон ≈ 103 ЧГ/год) при ε ≤ 1e-4 даёт скачок < 0.02; запас 0.1.
        private const val CONTINUITY_TOLERANCE = 0.1
    }
}
