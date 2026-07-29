package app.pawclock.calculator

import app.pawclock.model.HorseType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * TDD-тесты 3-фазной формулы лошади (AAEP, §4.10 спецификации).
 *
 * Эталонные значения из плана Plan 2 (Task 9) / §4.10:
 *  - 1 г. = 6.5 ЧГ
 *  - 2 г. = 13 ЧГ
 *  - 3 г. = 18 ЧГ
 *  - 4 г. = 20.5 ЧГ
 *  - 10 л. = 20.5 + 2.5·6 = 35.5 ЧГ
 *  - 25 л. = 20.5 + 2.5·21 = 73 ЧГ
 *
 * Формула едина для всех типов — порода влияет только на оценку ЧЖ.
 */
class HorseAgeCalculatorTest {
    private val calculator = HorseAgeCalculator

    @Test
    fun `1 year horse equals 6 point 5 human years`() {
        assertEquals(6.5, calculator.toHumanYears(1.0), 0.01)
    }

    @Test
    fun `2 years horse equals 13 human years`() {
        assertEquals(13.0, calculator.toHumanYears(2.0), 0.01)
    }

    @Test
    fun `3 years horse equals 18 human years`() {
        assertEquals(18.0, calculator.toHumanYears(3.0), 0.01)
    }

    @Test
    fun `4 years horse equals 20 point 5 human years`() {
        assertEquals(20.5, calculator.toHumanYears(4.0), 0.01)
    }

    @Test
    fun `10 years horse equals 35 point 5 human years`() {
        // 20.5 + 2.5·6 = 35.5
        assertEquals(35.5, calculator.toHumanYears(10.0), 0.01)
    }

    @Test
    fun `25 years horse equals 73 human years`() {
        // 20.5 + 2.5·21 = 73
        assertEquals(73.0, calculator.toHumanYears(25.0), 0.01)
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
        val viaParams = calculator.toHumanYears(10.0, SpeciesParams.Horse(HorseType.LightHorse))
        val viaOverload = calculator.toHumanYears(10.0)
        assertEquals(viaOverload, viaParams, 1e-9)
        assertEquals(35.5, viaParams, 0.01)
    }

    @Test
    fun `rejects non-Horse params`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(10.0, SpeciesParams.Rat)
        }
    }

    @ParameterizedTest(name = "Horse at {0} years = {1} human years")
    @CsvSource(
        "0.5, 3.25", // полугодовалый жеребёнок: 6.5·0.5
        "1.0, 6.5", // стык сегментов 1 и 2
        "1.5, 9.75", // 6.5 + 6.5·0.5
        "2.0, 13.0", // стык сегментов 2 и 3
        "2.5, 15.5", // 13 + 5·0.5
        "3.0, 18.0", // стык сегментов 3 и 4
        "4.0, 20.5",
        "10.0, 35.5",
        "20.0, 60.5",
        "25.0, 73.0",
    )
    fun `human years for table ages`(
        horseAge: Double,
        expectedHuman: Double,
    ) {
        assertEquals(expectedHuman, calculator.toHumanYears(horseAge), 0.01)
    }

    @Test
    fun `result is continuous at segment boundaries`() {
        // На стыке 1 года оба сегмента дают 6.5 ЧГ.
        assertEquals(6.5, calculator.toHumanYears(1.0), 0.01)
        assertEquals(calculator.toHumanYears(1.0), calculator.toHumanYears(0.9999), 0.01)
        // На стыке 2 лет — 13 ЧГ.
        assertEquals(13.0, calculator.toHumanYears(2.0), 0.01)
        assertEquals(calculator.toHumanYears(2.0), calculator.toHumanYears(1.9999), 0.01)
        // На стыке 3 лет — 18 ЧГ.
        assertEquals(18.0, calculator.toHumanYears(3.0), 0.01)
        assertEquals(calculator.toHumanYears(3.0), calculator.toHumanYears(2.9999), 0.01)
    }

    @Test
    fun `result is monotonically increasing in age`() {
        var previous = Double.NEGATIVE_INFINITY
        for (ageHundredths in 1..3000) {
            val age = ageHundredths / 100.0
            val current = calculator.toHumanYears(age)
            assert(current > previous) {
                "Non-monotonic at age=$age: previous=$previous, current=$current"
            }
            previous = current
        }
    }
}
