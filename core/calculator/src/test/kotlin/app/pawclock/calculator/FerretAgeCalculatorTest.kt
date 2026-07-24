package app.pawclock.calculator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * TDD-тесты кусочной формулы хорька (PMC «Senior Ferret», §4.7 спецификации).
 *
 * Эталонные значения из плана Plan 2 (Task 6):
 *  - 2 мес. (0.167 г.) = +5·2 = 10 ЧГ
 *  - 6 мес. (0.5 г.) = 30 ЧГ
 *  - 1 г. = 40 ЧГ
 *  - 3 г. = 40 + 4·2 = 48 ЧГ
 *  - 7 л. = 40 + 4·6 = 64 ЧГ
 *
 * Формула едина для всех — у хорька нет подкатегорий.
 */
class FerretAgeCalculatorTest {
    private val calculator = FerretAgeCalculator

    @Test
    fun `2 months ferret equals 10 human years`() {
        // 60 · (2/12) = 10
        val result = calculator.toHumanYears(ageInYears = 2.0 / 12.0)
        assertEquals(10.0, result, 0.01)
    }

    @Test
    fun `6 months ferret equals 30 human years`() {
        val result = calculator.toHumanYears(ageInYears = 0.5)
        assertEquals(30.0, result, 0.01)
    }

    @Test
    fun `1 year ferret equals 40 human years`() {
        val result = calculator.toHumanYears(ageInYears = 1.0)
        assertEquals(40.0, result, 0.01)
    }

    @Test
    fun `3 years ferret equals 48 human years`() {
        // 40 + 4·2 = 48
        val result = calculator.toHumanYears(ageInYears = 3.0)
        assertEquals(48.0, result, 0.01)
    }

    @Test
    fun `7 years ferret equals 64 human years`() {
        // 40 + 4·6 = 64
        val result = calculator.toHumanYears(ageInYears = 7.0)
        assertEquals(64.0, result, 0.01)
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
        val viaParams = calculator.toHumanYears(3.0, SpeciesParams.Ferret)
        val viaOverload = calculator.toHumanYears(3.0)
        assertEquals(viaOverload, viaParams, 1e-9)
        assertEquals(48.0, viaParams, 0.01)
    }

    @Test
    fun `rejects non-Ferret params`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(3.0, SpeciesParams.Rat)
        }
    }

    @ParameterizedTest(name = "Ferret at {0} years = {1} human years")
    @CsvSource(
        "0.0833, 5.0", // 1 мес.: 60·0.0833
        "0.1667, 10.0", // 2 мес.
        "0.25, 15.0", // 3 мес.
        "0.5, 30.0", // 6 мес. (стык сегментов 1 и 2)
        "0.75, 35.0", // 9 мес.: 30 + 20·0.25
        "1.0, 40.0", // 1 год (стык сегментов 2 и 3)
        "2.0, 44.0",
        "3.0, 48.0",
        "5.0, 56.0",
        "7.0, 64.0",
    )
    fun `human years for table ages`(
        ferretAge: Double,
        expectedHuman: Double,
    ) {
        val result = calculator.toHumanYears(ferretAge)
        assertEquals(expectedHuman, result, 0.01)
    }

    @Test
    fun `result is continuous at segment boundaries`() {
        // На стыке 6 мес.: оба сегмента дают 30 ЧГ.
        val justBeforeSixMonths = calculator.toHumanYears(0.4999)
        val atSixMonths = calculator.toHumanYears(0.5)
        assertEquals(30.0, atSixMonths, 0.01)
        assertEquals(atSixMonths, justBeforeSixMonths, 0.01)
        // На стыке 1 года: оба сегмента дают 40 ЧГ.
        val justBeforeOneYear = calculator.toHumanYears(0.9999)
        val atOneYear = calculator.toHumanYears(1.0)
        assertEquals(40.0, atOneYear, 0.01)
        assertEquals(atOneYear, justBeforeOneYear, 0.01)
    }

    @Test
    fun `result is monotonically increasing in age`() {
        var previous = Double.NEGATIVE_INFINITY
        for (ageHundredths in 1..1000) {
            val age = ageHundredths / 100.0
            val current = calculator.toHumanYears(age)
            assert(current > previous) {
                "Non-monotonic at age=$age: previous=$previous, current=$current"
            }
            previous = current
        }
    }
}
