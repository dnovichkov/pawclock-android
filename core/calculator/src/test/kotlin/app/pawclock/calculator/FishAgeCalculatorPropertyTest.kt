@file:OptIn(io.kotest.common.ExperimentalKotest::class)

package app.pawclock.calculator

import app.pawclock.model.FishType
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.double
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Property-based проверки [FishAgeCalculator] (PetMD + AquariumStoreDepot, §4.11; см. §11.6).
 *
 * Чисто скалярная формула `ЧГ = age · 80 / lifespan` (без поправки молодняка, как у рептилии):
 *  1. **Monotonicity:** для любого [FishType] функция строго возрастает (линейна, наклон > 0).
 *  2. **Ratio invariant:** для любого `age > 0` выполняется `f(age) / age == 80 / lifespan` —
 *     определяющее свойство скалярной модели §4.11 (см. [ScalarRatioFormula]).
 *  3. **Positivity:** результат > 0 для любого `age > 0`.
 *
 * Источники: PetMD; Kodama Koi Farm; AquariumStoreDepot. См. §4.11 и ADR-0006.
 */
class FishAgeCalculatorPropertyTest {
    private val calculator = FishAgeCalculator

    private val config = PropTestConfig(iterations = PROPERTY_ITERATIONS)

    @Test
    fun `fish age is monotonically non-decreasing for every type`() =
        runBlocking {
            FishType.entries.forEach { type ->
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
    fun `fish ratio equals 80 over lifespan for every type`() =
        runBlocking {
            FishType.entries.forEach { type ->
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
    fun `fish age is strictly positive for positive input and every type`() =
        runBlocking {
            FishType.entries.forEach { type ->
                checkAll(config, Arb.double(MIN_AGE, MAX_REASONABLE_AGE)) { age ->
                    val result = calculator.toHumanYears(age, type)
                    assertTrue(result > 0.0, "$type at age=$age: $result not positive")
                }
            }
        }

    @Test
    fun `fish throws on zero or negative ages`() =
        runBlocking {
            checkAll(config, Arb.double(-MAX_REASONABLE_AGE, 0.0)) { age ->
                val result = runCatching { calculator.toHumanYears(age, FishType.Goldfish) }
                assertTrue(
                    result.isFailure && result.exceptionOrNull() is IllegalArgumentException,
                    "Expected IllegalArgumentException for age=$age, got $result",
                )
            }
        }

    private companion object {
        private const val PROPERTY_ITERATIONS = 200
        private const val MIN_AGE = 0.01

        // Максимальный реальный возраст долгоживущих кои ≈ 40 лет (с запасом).
        private const val MAX_REASONABLE_AGE = 40.0

        // Отношение f(age)/age — точная константа, допускаем лишь погрешность double.
        private const val RATIO_TOLERANCE = 1.0e-9
    }
}
