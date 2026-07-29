@file:OptIn(io.kotest.common.ExperimentalKotest::class)

package app.pawclock.calculator

import app.pawclock.model.HamsterType
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.double
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Property-based проверки [HamsterAgeCalculator] (RVC VetCompass + Animallama, §4.4; см. §11.6).
 *
 * Свойства:
 *  1. **Monotonicity:** для любого [HamsterType] функция `age → humanYears` строго возрастает.
 *  2. **Continuity:** в отличие от §4.3 (кролик), формула §4.4 **непрерывна** на всех стыках
 *     (1, 2, 18 мес.) — сегменты подобраны так, чтобы значения совпадали. Проверяем, что
 *     `|f(b−ε) − f(b+ε)|` мало для каждого стыка (отсутствие скачка).
 *  3. **Positivity:** результат > 0 для любого `age > 0`.
 *
 * Источники: RVC VetCompass; Animallama hamster age chart. См. §4.4 и ADR-0006.
 */
class HamsterAgeCalculatorPropertyTest {
    private val calculator = HamsterAgeCalculator

    private val config = PropTestConfig(iterations = PROPERTY_ITERATIONS)

    @Test
    fun `hamster age is monotonically non-decreasing for every type`() =
        runBlocking {
            HamsterType.entries.forEach { type ->
                checkAll(
                    config,
                    Arb.double(MIN_AGE, MAX_REASONABLE_AGE),
                    Arb.double(MIN_AGE, MAX_REASONABLE_AGE),
                ) { a, b ->
                    val (lo, hi) = if (a <= b) a to b else b to a
                    val loYears = calculator.toHumanYears(lo, type)
                    val hiYears = calculator.toHumanYears(hi, type)
                    assertTrue(
                        loYears <= hiYears,
                        "$type: monotonicity violated f($lo)=$loYears > f($hi)=$hiYears",
                    )
                }
            }
        }

    @Test
    fun `hamster formula is continuous at piecewise boundaries`() =
        runBlocking {
            val months = HamsterAgeCalculator.MONTHS_PER_YEAR
            val boundaries =
                listOf(
                    HamsterAgeCalculator.ONE_MONTH / months,
                    HamsterAgeCalculator.TWO_MONTHS / months,
                    HamsterAgeCalculator.EIGHTEEN_MONTHS / months,
                )
            checkAll(config, Arb.double(SMALL_EPS_MIN, SMALL_EPS_MAX)) { eps ->
                boundaries.forEach { boundary ->
                    val left = calculator.toHumanYears(boundary - eps, HamsterType.Syrian)
                    val right = calculator.toHumanYears(boundary + eps, HamsterType.Syrian)
                    assertTrue(
                        kotlin.math.abs(right - left) < CONTINUITY_TOLERANCE,
                        "Discontinuity at $boundary (eps=$eps): left=$left right=$right",
                    )
                }
            }
        }

    @Test
    fun `hamster age is strictly positive for positive input`() =
        runBlocking {
            HamsterType.entries.forEach { type ->
                checkAll(config, Arb.double(MIN_AGE, MAX_REASONABLE_AGE)) { age ->
                    val result = calculator.toHumanYears(age, type)
                    assertTrue(result > 0.0, "$type at age=$age: $result not positive")
                }
            }
        }

    @Test
    fun `hamster throws on zero or negative ages`() =
        runBlocking {
            checkAll(config, Arb.double(-MAX_REASONABLE_AGE, 0.0)) { age ->
                val result = runCatching { calculator.toHumanYears(age, HamsterType.Syrian) }
                assertTrue(
                    result.isFailure && result.exceptionOrNull() is IllegalArgumentException,
                    "Expected IllegalArgumentException for age=$age, got $result",
                )
            }
        }

    private companion object {
        private const val PROPERTY_ITERATIONS = 300
        private const val MIN_AGE = 0.01

        // Максимальный реальный возраст хомяка ≈ 4 года (с большим запасом).
        private const val MAX_REASONABLE_AGE = 4.0

        private const val SMALL_EPS_MIN = 1.0e-6
        private const val SMALL_EPS_MAX = 1.0e-4

        // При ε ≤ 1e-4 и максимальном наклоне ≈ 48 ЧГ/год разница на стыке < 0.01; запас до 0.1.
        private const val CONTINUITY_TOLERANCE = 0.1
    }
}
