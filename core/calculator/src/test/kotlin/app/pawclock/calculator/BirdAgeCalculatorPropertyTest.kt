@file:OptIn(io.kotest.common.ExperimentalKotest::class)

package app.pawclock.calculator

import app.pawclock.model.BirdType
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.double
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Property-based проверки [BirdAgeCalculator] (AAV + Lafeber Vet, §4.8; см. §11.6).
 *
 * Скалярная формула `ЧГ = age · 80 / lifespan` с поправкой × 1.3 для возраста < 6 мес.:
 *  1. **Monotonicity по сегментам:** функция возрастает **по обе стороны** от порога 0.5 г.,
 *     но НЕ глобально — на самом пороге множитель отключается, создавая намеренный скачок
 *     вниз ровно в 1.3 раза. Поэтому свойство проверяется только для пар по одну сторону 0.5.
 *  2. **1.3× correction:** на пороге `f(0.5−ε) / f(0.5+ε) → 1.3` (отключение поправки птенца).
 *  3. **Positivity:** результат > 0 для любого `age > 0` и любого [BirdType].
 *
 * Источники: AAV «Care for Senior Parrots»; Lafeber Vet. См. §4.8 и ADR-0006.
 */
class BirdAgeCalculatorPropertyTest {
    private val calculator = BirdAgeCalculator

    private val config = PropTestConfig(iterations = PROPERTY_ITERATIONS)

    @Test
    fun `bird age is monotonically non-decreasing within each side of the juvenile threshold`() =
        runBlocking {
            val threshold = BirdAgeCalculator.JUVENILE_AGE_THRESHOLD
            BirdType.entries.forEach { type ->
                checkAll(
                    config,
                    Arb.double(MIN_AGE, MAX_REASONABLE_AGE),
                    Arb.double(MIN_AGE, MAX_REASONABLE_AGE),
                ) { a, b ->
                    val (lo, hi) = if (a <= b) a to b else b to a
                    // Пропускаем пары, пересекающие порог: на нём поправка ×1.3 отключается
                    // (намеренный скачок вниз — проверяется отдельным тестом).
                    if ((lo < threshold) == (hi < threshold)) {
                        val loYears = calculator.toHumanYears(lo, type)
                        val hiYears = calculator.toHumanYears(hi, type)
                        assertTrue(
                            loYears <= hiYears,
                            "$type: monotonicity violated f($lo)=$loYears > f($hi)=$hiYears",
                        )
                    }
                }
            }
        }

    @Test
    fun `bird juvenile correction is exactly 1_3 at the threshold`() =
        runBlocking {
            val threshold = BirdAgeCalculator.JUVENILE_AGE_THRESHOLD
            BirdType.entries.forEach { type ->
                checkAll(config, Arb.double(SMALL_EPS_MIN, SMALL_EPS_MAX)) { eps ->
                    val justBelow = calculator.toHumanYears(threshold - eps, type)
                    val atOrAbove = calculator.toHumanYears(threshold + eps, type)
                    val ratio = justBelow / atOrAbove
                    assertTrue(
                        kotlin.math.abs(ratio - BirdAgeCalculator.JUVENILE_MULTIPLIER) < RATIO_TOLERANCE,
                        "$type: correction ratio at threshold = $ratio, expected ≈ 1.3 (eps=$eps)",
                    )
                }
            }
        }

    @Test
    fun `bird age is strictly positive for positive input and every type`() =
        runBlocking {
            BirdType.entries.forEach { type ->
                checkAll(config, Arb.double(MIN_AGE, MAX_REASONABLE_AGE)) { age ->
                    val result = calculator.toHumanYears(age, type)
                    assertTrue(result > 0.0, "$type at age=$age: $result not positive")
                }
            }
        }

    @Test
    fun `bird throws on zero or negative ages`() =
        runBlocking {
            checkAll(config, Arb.double(-MAX_REASONABLE_AGE, 0.0)) { age ->
                val result = runCatching { calculator.toHumanYears(age, BirdType.Budgerigar) }
                assertTrue(
                    result.isFailure && result.exceptionOrNull() is IllegalArgumentException,
                    "Expected IllegalArgumentException for age=$age, got $result",
                )
            }
        }

    private companion object {
        private const val PROPERTY_ITERATIONS = 200
        private const val MIN_AGE = 0.01

        // Максимальный реальный возраст долгоживущих попугаев ≈ 80 лет (с запасом).
        private const val MAX_REASONABLE_AGE = 80.0

        private const val SMALL_EPS_MIN = 1.0e-6
        private const val SMALL_EPS_MAX = 1.0e-4

        // На ε → 0 отношение f(0.5−ε)/f(0.5+ε) → 1.3·(0.5−ε)/(0.5+ε); при ε ≤ 1e-4 отклонение < 1e-3.
        private const val RATIO_TOLERANCE = 1.0e-3
    }
}
