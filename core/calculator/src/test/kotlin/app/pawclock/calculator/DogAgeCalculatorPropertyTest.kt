@file:OptIn(io.kotest.common.ExperimentalKotest::class)

package app.pawclock.calculator

import app.pawclock.model.CalculationMethod
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.double
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Property-based проверки эпигенетической формулы Wang T. et al. 2020 (см. §11.6 спецификации).
 *
 * Свойства, которые мы заявляем:
 *  1. **Monotonicity:** если `age1 < age2`, то `toHumanYears(age1) ≤ toHumanYears(age2)`
 *     (биологически возраст не может уменьшаться).
 *  2. **Positivity:** для любого `age > 0` результат `> 0` (отрицательного возраста не бывает).
 *  3. **Boundedness:** для разумного диапазона возрастов собак (≤ 30 лет, что превышает
 *     даже задокументированный мировой рекорд 29.5 лет) результат заведомо `< 200` ЧГ.
 *
 * Эти инварианты ловят регрессии, которые легко пропустить в табличных тестах:
 *  - случайный знак минус в коэффициенте формулы,
 *  - разрыв между puppy-расширением и Wang-формулой в точке `age = 1`,
 *  - переполнение при больших age.
 *
 * Источник формулы: Wang T. et al. Cell Systems 2020, DOI: 10.1016/j.cels.2020.06.006.
 */
class DogAgeCalculatorPropertyTest {
    private val calculator = DogAgeCalculator()

    private val config = PropTestConfig(iterations = PROPERTY_ITERATIONS)

    @Test
    fun `Wang formula is monotonically non-decreasing in age`() =
        runBlocking {
            checkAll(
                config,
                Arb.double(MIN_AGE, MAX_REASONABLE_AGE),
                Arb.double(MIN_AGE, MAX_REASONABLE_AGE),
            ) { a, b ->
                val (lo, hi) = if (a <= b) a to b else b to a
                val loYears = calculator.toHumanYears(lo, CalculationMethod.EPIGENETIC)
                val hiYears = calculator.toHumanYears(hi, CalculationMethod.EPIGENETIC)
                assertTrue(
                    loYears <= hiYears,
                    "Expected monotonic non-decrease: f($lo)=$loYears > f($hi)=$hiYears",
                )
            }
        }

    @Test
    fun `Wang formula result is always strictly positive for positive input`() =
        runBlocking {
            checkAll(config, Arb.double(MIN_AGE, MAX_REASONABLE_AGE)) { age ->
                val result = calculator.toHumanYears(age, CalculationMethod.EPIGENETIC)
                assertTrue(result > 0.0, "Expected positive result for age=$age, got $result")
            }
        }

    @Test
    fun `Wang formula result is bounded above by 200 human years for dogs up to 30 years`() =
        runBlocking {
            // Даже мировой рекорд (Bluey, 29.5 лет) → 16·ln(29.5)+31 ≈ 85.2 ЧГ.
            // Граница 200 ловит регрессии с неверной базой логарифма или умножением вместо сложения.
            checkAll(config, Arb.double(MIN_AGE, MAX_REASONABLE_AGE)) { age ->
                val result = calculator.toHumanYears(age, CalculationMethod.EPIGENETIC)
                assertTrue(result < UPPER_BOUND_HUMAN_YEARS, "Age $age → $result ≥ 200 ЧГ")
            }
        }

    @Test
    fun `Wang puppy extension is continuous with Wang formula at age 1`() =
        runBlocking {
            // Это property-проверка непрерывности: ε-окрестность age=1 не даёт скачка.
            // Гарантирует, что переключение ветвей `age < 1` ↔ `age >= 1` бесшовно.
            checkAll(config, Arb.double(SMALL_EPS, SMALL_EPS * EPS_MULTIPLIER)) { eps ->
                val left = calculator.toHumanYears(1.0 - eps, CalculationMethod.EPIGENETIC)
                val right = calculator.toHumanYears(1.0 + eps, CalculationMethod.EPIGENETIC)
                // На малой окрестности значения должны быть близки к 31.0 ± tolerance.
                assertEquals(WANG_AT_AGE_ONE, left, CONTINUITY_TOLERANCE)
                assertEquals(WANG_AT_AGE_ONE, right, CONTINUITY_TOLERANCE)
            }
        }

    @Test
    fun `Wang formula throws on zero or negative ages for any sample`() =
        runBlocking {
            checkAll(config, Arb.double(-MAX_REASONABLE_AGE, 0.0)) { age ->
                runCatching { calculator.toHumanYears(age, CalculationMethod.EPIGENETIC) }
                    .also {
                        assertTrue(
                            it.isFailure && it.exceptionOrNull() is IllegalArgumentException,
                            "Expected IllegalArgumentException for age=$age, got $it",
                        )
                    }
            }
        }

    private companion object {
        private const val PROPERTY_ITERATIONS = 500
        private const val MIN_AGE = 0.01
        private const val MAX_REASONABLE_AGE = 30.0
        private const val UPPER_BOUND_HUMAN_YEARS = 200.0
        private const val WANG_AT_AGE_ONE = 31.0

        // Малая ε и её множитель: окно ~[1e-4, 1e-2] вокруг age=1 для проверки непрерывности.
        private const val SMALL_EPS = 1.0e-4
        private const val EPS_MULTIPLIER = 100.0

        // Толеранс непрерывности: Wang' производная в age=1 = 16, поэтому на |eps|≤0.01
        // изменение ≈ 0.16; даём запас до 0.5.
        private const val CONTINUITY_TOLERANCE = 0.5
    }
}
