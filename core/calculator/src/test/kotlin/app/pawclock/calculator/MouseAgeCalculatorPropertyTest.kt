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
 * Property-based проверки [MouseAgeCalculator] (Dutta & Sengupta 2016, §4.6; см. §11.6).
 *
 * Модель — кусочная интеграция «человеко-дней на мышиный день» по 5 фазам:
 *  1. **Monotonicity:** функция строго возрастает (все скорости старения положительны).
 *  2. **Continuity:** накопление непрерывно на всех 4 границах фаз (42, 180, 365, 730 дней) —
 *     каждый сегмент стартует с накопленной суммы предыдущего. Проверяем отсутствие скачка.
 *  3. **Positivity:** результат > 0 для любого `age > 0`.
 *
 * Источник: Dutta S. & Sengupta P. (2016), Life Sciences 152:244–248,
 * DOI: 10.1016/j.lfs.2015.10.025. См. §4.6 и ADR-0006.
 */
class MouseAgeCalculatorPropertyTest {
    private val calculator = MouseAgeCalculator

    private val config = PropTestConfig(iterations = PROPERTY_ITERATIONS)

    @Test
    fun `mouse age is monotonically non-decreasing`() =
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
    fun `mouse formula is continuous at all four phase boundaries`() =
        runBlocking {
            val days = MouseAgeCalculator.DAYS_PER_YEAR
            val boundaries =
                listOf(
                    MouseAgeCalculator.PHASE_1_END / days,
                    MouseAgeCalculator.PHASE_2_END / days,
                    MouseAgeCalculator.PHASE_3_END / days,
                    MouseAgeCalculator.PHASE_4_END / days,
                )
            checkAll(config, Arb.double(SMALL_EPS_MIN, SMALL_EPS_MAX)) { eps ->
                boundaries.forEach { boundary ->
                    val left = calculator.toHumanYears(boundary - eps)
                    val right = calculator.toHumanYears(boundary + eps)
                    assertTrue(
                        kotlin.math.abs(right - left) < CONTINUITY_TOLERANCE,
                        "Discontinuity at $boundary y (eps=$eps): left=$left right=$right",
                    )
                }
            }
        }

    @Test
    fun `mouse age is strictly positive for positive input`() =
        runBlocking {
            checkAll(config, Arb.double(MIN_AGE, MAX_REASONABLE_AGE)) { age ->
                val result = calculator.toHumanYears(age)
                assertTrue(result > 0.0, "Expected positive result for age=$age, got $result")
            }
        }

    @Test
    fun `mouse throws on zero or negative ages`() =
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

        // Максимальный реальный возраст мыши ≈ 4 года (с запасом).
        private const val MAX_REASONABLE_AGE = 4.0

        private const val SMALL_EPS_MIN = 1.0e-6
        private const val SMALL_EPS_MAX = 1.0e-4

        // Самая крутая фаза (0–42 дн., наклон ≈ 150 ЧГ/год) при ε ≤ 1e-4 даёт скачок < 0.03; запас 0.1.
        private const val CONTINUITY_TOLERANCE = 0.1
    }
}
