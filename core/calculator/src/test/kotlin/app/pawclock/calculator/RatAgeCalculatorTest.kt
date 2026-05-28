package app.pawclock.calculator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * TDD-тесты линейной формулы Sengupta 2013 для крыс.
 *
 * Эталонные значения из спецификации PawClock §4.6 (и плана Plan 2, Task 5):
 * `ЧГ = 13.8 · age + 1.4`
 *  - 0.5 г. = 8.3 ЧГ
 *  - 1 г. = 15.2 ЧГ
 *  - 2 г. = 29.0 ЧГ
 *  - 3 г. = 42.8 ЧГ
 *
 * Формула едина для всех — у крысы нет подкатегорий.
 */
class RatAgeCalculatorTest {
    private val calculator = RatAgeCalculator

    @Test
    fun `half year rat equals 8_3 human years`() {
        // 13.8 · 0.5 + 1.4 = 8.3
        val result = calculator.toHumanYears(ageInYears = 0.5)
        assertEquals(8.3, result, 0.01)
    }

    @Test
    fun `1 year rat equals 15_2 human years`() {
        // 13.8 · 1 + 1.4 = 15.2
        val result = calculator.toHumanYears(ageInYears = 1.0)
        assertEquals(15.2, result, 0.01)
    }

    @Test
    fun `2 year rat equals 29 human years`() {
        // 13.8 · 2 + 1.4 = 29.0
        val result = calculator.toHumanYears(ageInYears = 2.0)
        assertEquals(29.0, result, 0.01)
    }

    @Test
    fun `3 year rat equals 42_8 human years`() {
        // 13.8 · 3 + 1.4 = 42.8
        val result = calculator.toHumanYears(ageInYears = 3.0)
        assertEquals(42.8, result, 0.01)
    }

    @Test
    fun `throws on zero age`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(0.0)
        }
    }

    @Test
    fun `throws on negative age`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(-1.0)
        }
    }

    @Test
    fun `unified toHumanYears via params matches direct overload`() {
        val viaParams = calculator.toHumanYears(2.0, SpeciesParams.Rat)
        val viaOverload = calculator.toHumanYears(2.0)
        assertEquals(viaOverload, viaParams, 1e-9)
        assertEquals(29.0, viaParams, 0.01)
    }

    @Test
    fun `rejects non-Rat params`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(2.0, SpeciesParams.Mouse)
        }
    }

    @ParameterizedTest(name = "Rat at {0} years = {1} human years")
    @CsvSource(
        "0.25, 4.85", // 13.8 · 0.25 + 1.4
        "0.5, 8.3",
        "1.0, 15.2",
        "1.5, 22.1",
        "2.0, 29.0",
        "2.5, 35.9",
        "3.0, 42.8",
    )
    fun `human years for table ages`(
        ratAge: Double,
        expectedHuman: Double,
    ) {
        val result = calculator.toHumanYears(ratAge)
        assertEquals(expectedHuman, result, 0.01)
    }

    @Test
    fun `result is monotonically increasing in age`() {
        var previous = Double.NEGATIVE_INFINITY
        for (ageHundredths in 1..400) {
            val age = ageHundredths / 100.0
            val current = calculator.toHumanYears(age)
            assert(current > previous) {
                "Non-monotonic at age=$age: previous=$previous, current=$current"
            }
            previous = current
        }
    }
}
