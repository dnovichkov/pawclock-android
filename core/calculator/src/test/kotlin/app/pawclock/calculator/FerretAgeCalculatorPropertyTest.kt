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
 * Property-based проверки [FerretAgeCalculator] (PMC «Senior Ferret» + Oxbow, §4.7; см. §11.6).
 *
 * У хорька нет подкатегорий — формула едина:
 *  1. **Monotonicity:** функция строго возрастает (3 непрерывных сегмента, наклоны 60 → 20 → 4).
 *  2. **Continuity:** формула §4.7 **непрерывна** на стыках `age = 0.5` (→ 30 ЧГ в обоих сегментах)
 *     и `age = 1.0` (→ 40 ЧГ). Проверяем отсутствие скачка.
 *  3. **Positivity:** результат > 0 для любого `age > 0`.
 *
 * Источники: Church R. R. PMC7129291; Oxbow Ferret Life Stages. См. §4.7 и ADR-0006.
 */
class FerretAgeCalculatorPropertyTest {
    private val calculator = FerretAgeCalculator

    private val config = PropTestConfig(iterations = PROPERTY_ITERATIONS)

    @Test
    fun `ferret age is monotonically non-decreasing`() =
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
    fun `ferret formula is continuous at piecewise boundaries`() =
        runBlocking {
            val boundaries = listOf(SIX_MONTHS, ONE_YEAR)
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
    fun `ferret age is strictly positive for positive input`() =
        runBlocking {
            checkAll(config, Arb.double(MIN_AGE, MAX_REASONABLE_AGE)) { age ->
                val result = calculator.toHumanYears(age)
                assertTrue(result > 0.0, "Expected positive result for age=$age, got $result")
            }
        }

    @Test
    fun `ferret throws on zero or negative ages`() =
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

        // Максимальный реальный возраст хорька ≈ 12 лет (с запасом).
        private const val MAX_REASONABLE_AGE = 12.0

        // Стыки сегментов §4.7 (константы калькулятора private — дублируем как литералы здесь).
        private const val SIX_MONTHS = 0.5
        private const val ONE_YEAR = 1.0

        private const val SMALL_EPS_MIN = 1.0e-6
        private const val SMALL_EPS_MAX = 1.0e-4

        // При ε ≤ 1e-4 и наклоне ≤ 60 ЧГ/год скачок < 0.012; запас 0.1.
        private const val CONTINUITY_TOLERANCE = 0.1
    }
}
