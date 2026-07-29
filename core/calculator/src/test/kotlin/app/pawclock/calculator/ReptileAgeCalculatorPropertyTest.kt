@file:OptIn(io.kotest.common.ExperimentalKotest::class)

package app.pawclock.calculator

import app.pawclock.model.ReptileType
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.double
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Property-based проверки [ReptileAgeCalculator] (PetPlace + Reptile Centre, §4.9; см. §11.6).
 *
 * Чисто скалярная формула `ЧГ = age · 80 / lifespan` (без поправки молодняка):
 *  1. **Monotonicity:** для любого [ReptileType] функция строго возрастает (линейна, наклон > 0).
 *  2. **Ratio invariant:** для любого `age > 0` выполняется `f(age) / age == 80 / lifespan` —
 *     это определяющее свойство скалярной модели §4.9 (см. [ScalarRatioFormula]).
 *  3. **Positivity:** результат > 0 для любого `age > 0`.
 *
 * Источники: PetPlace; Reptile Centre; A-Z Animals. См. §4.9 и ADR-0006.
 */
class ReptileAgeCalculatorPropertyTest {
    private val calculator = ReptileAgeCalculator

    private val config = PropTestConfig(iterations = PROPERTY_ITERATIONS)

    @Test
    fun `reptile age is monotonically non-decreasing for every type`() =
        runBlocking {
            ReptileType.entries.forEach { type ->
                checkAll(
                    config,
                    Arb.double(MIN_AGE, MAX_REASONABLE_AGE),
                    Arb.double(MIN_AGE, MAX_REASONABLE_AGE),
                ) { a, b ->
                    val (lo, hi) = if (a <= b) a to b else b to a
                    assertTrue(
                        calculator.toHumanYears(lo, type) <= calculator.toHumanYears(hi, type),
                        "$type: monotonicity violated f($lo) > f($hi)",
                    )
                }
            }
        }

    @Test
    fun `reptile ratio equals 80 over lifespan for every type`() =
        runBlocking {
            ReptileType.entries.forEach { type ->
                val expectedRatio = ScalarRatioFormula.HUMAN_REFERENCE_LIFESPAN / type.averageLifespanYears
                checkAll(config, Arb.double(MIN_AGE, MAX_REASONABLE_AGE)) { age ->
                    val ratio = calculator.toHumanYears(age, type) / age
                    assertTrue(
                        kotlin.math.abs(ratio - expectedRatio) < RATIO_TOLERANCE,
                        "$type at age=$age: ratio=$ratio, expected=$expectedRatio",
                    )
                }
            }
        }

    @Test
    fun `reptile age is strictly positive for positive input and every type`() =
        runBlocking {
            ReptileType.entries.forEach { type ->
                checkAll(config, Arb.double(MIN_AGE, MAX_REASONABLE_AGE)) { age ->
                    val result = calculator.toHumanYears(age, type)
                    assertTrue(result > 0.0, "$type at age=$age: $result not positive")
                }
            }
        }

    @Test
    fun `reptile throws on zero or negative ages`() =
        runBlocking {
            checkAll(config, Arb.double(-MAX_REASONABLE_AGE, 0.0)) { age ->
                val result = runCatching { calculator.toHumanYears(age, ReptileType.BoxTurtle) }
                assertTrue(
                    result.isFailure && result.exceptionOrNull() is IllegalArgumentException,
                    "Expected IllegalArgumentException for age=$age, got $result",
                )
            }
        }

    private companion object {
        private const val PROPERTY_ITERATIONS = 200
        private const val MIN_AGE = 0.01

        // Максимальный реальный возраст долгоживущих черепах ≈ 60 лет (с запасом).
        private const val MAX_REASONABLE_AGE = 60.0

        // Отношение f(age)/age — точная константа, допускаем лишь погрешность double.
        private const val RATIO_TOLERANCE = 1.0e-9
    }
}
