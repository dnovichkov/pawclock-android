package app.pawclock.calculator

import app.pawclock.model.RabbitSize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * TDD-тесты кусочной формулы House Rabbit Society + AVMA для кроликов.
 *
 * Эталонные значения взяты из спецификации PawClock §4.3.
 *
 * Кусочная формула:
 * ```
 * 0–4 мес. (age < 0.33):     ЧГ = 30 · age
 * 4–12 мес. (0.33 ≤ age < 1): ЧГ = 12 + 8 · (age − 0.33)
 * 1+ год (age ≥ 1):           ЧГ = 21 + 6 · (age − 1)
 * ```
 *
 * Формула едина для всех пород ([RabbitSize] на возраст не влияет).
 *
 * Источник: House Rabbit Society guidelines + AVMA. См. §4.3 спецификации.
 */
class RabbitAgeCalculatorTest {
    private val calculator = RabbitAgeCalculator

    @Test
    fun `0_1 year rabbit equals 3 human years`() {
        // 30 · 0.1 = 3 (первый сегмент)
        val result = calculator.toHumanYears(ageInYears = 0.1, size = RabbitSize.Medium)
        assertEquals(3.0, result, 0.01)
    }

    @Test
    fun `4 months rabbit equals 12 human years at boundary`() {
        // Граница первого сегмента: 12 + 8 · (0.33 − 0.33) = 12
        val result = calculator.toHumanYears(ageInYears = 0.33, size = RabbitSize.Medium)
        assertEquals(12.0, result, 0.01)
    }

    @Test
    fun `0_5 year rabbit equals about 13_36 human years`() {
        // 12 + 8 · (0.5 − 0.33) = 13.36
        val result = calculator.toHumanYears(ageInYears = 0.5, size = RabbitSize.Medium)
        assertEquals(13.36, result, 0.01)
    }

    @Test
    fun `1 year rabbit equals 21 human years`() {
        val result = calculator.toHumanYears(ageInYears = 1.0, size = RabbitSize.Medium)
        assertEquals(21.0, result, 0.01)
    }

    @Test
    fun `2 year rabbit equals 27 human years`() {
        // 21 + 6 · (2 − 1) = 27
        val result = calculator.toHumanYears(ageInYears = 2.0, size = RabbitSize.Medium)
        assertEquals(27.0, result, 0.01)
    }

    @Test
    fun `5 year rabbit equals 45 human years`() {
        // 21 + 6 · (5 − 1) = 45
        val result = calculator.toHumanYears(ageInYears = 5.0, size = RabbitSize.Medium)
        assertEquals(45.0, result, 0.01)
    }

    @Test
    fun `8 year rabbit equals 63 human years`() {
        // 21 + 6 · (8 − 1) = 63
        val result = calculator.toHumanYears(ageInYears = 8.0, size = RabbitSize.Medium)
        assertEquals(63.0, result, 0.01)
    }

    @Test
    fun `size does not affect human years`() {
        // Формула House Rabbit Society едина для всех пород.
        val medium = calculator.toHumanYears(5.0, RabbitSize.Medium)
        for (size in RabbitSize.entries) {
            assertEquals(medium, calculator.toHumanYears(5.0, size), 0.01)
        }
    }

    @Test
    fun `throws on zero age`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(0.0, RabbitSize.Medium)
        }
    }

    @Test
    fun `throws on negative age`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(-1.0, RabbitSize.Medium)
        }
    }

    @Test
    fun `unified toHumanYears via params matches size overload`() {
        val params = SpeciesParams.Rabbit(size = RabbitSize.Dwarf)
        val viaParams = calculator.toHumanYears(5.0, params)
        val viaOverload = calculator.toHumanYears(5.0, RabbitSize.Dwarf)
        assertEquals(viaOverload, viaParams, 1e-9)
        assertEquals(45.0, viaParams, 0.01)
    }

    @Test
    fun `rejects non-Rabbit params`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(5.0, SpeciesParams.Cat(app.pawclock.model.CatType.IndoorShortHair))
        }
    }

    @ParameterizedTest(name = "Rabbit at {0} years ≈ {1} human years")
    @CsvSource(
        "0.05, 1.5", // 30 · 0.05
        "0.1, 3.0", // 30 · 0.1
        "0.25, 7.5", // 30 · 0.25 (3 месяца, всё ещё первый сегмент)
        "0.33, 12.0", // граница → второй сегмент
        "0.5, 13.36", // 12 + 8 · 0.17
        "0.75, 15.36", // 12 + 8 · 0.42
        "1.0, 21.0", // граница → третий сегмент
        "2.0, 27.0", // 21 + 6
        "3.0, 33.0", // 21 + 12
        "5.0, 45.0", // 21 + 24
        "8.0, 63.0", // 21 + 42
        "10.0, 75.0", // 21 + 54
    )
    fun `human years for table ages`(
        rabbitAge: Double,
        expectedHuman: Double,
    ) {
        val result = calculator.toHumanYears(rabbitAge, RabbitSize.Medium)
        assertEquals(expectedHuman, result, 0.01)
    }

    @Test
    fun `result is monotonically increasing in age`() {
        var previous = Double.NEGATIVE_INFINITY
        for (ageHundredths in 1..3000) {
            val age = ageHundredths / 100.0
            val current = calculator.toHumanYears(age, RabbitSize.Medium)
            assert(current > previous) {
                "Non-monotonic at age=$age: previous=$previous, current=$current"
            }
            previous = current
        }
    }
}
