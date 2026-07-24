package app.pawclock.calculator

import app.pawclock.model.HamsterType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * TDD-тесты кусочной формулы RVC VetCompass + Animallama для хомяков.
 *
 * Эталонные значения из спецификации PawClock §4.4.
 *
 * Кусочная формула (в месяцах, m = age·12):
 * ```
 * 0–1 мес. (m ≤ 1):     ЧГ = 7.5 · m            (1 ЧГ за 4 дня, 30-дневный месяц)
 * 1–2 мес. (1 < m ≤ 2): ЧГ = 7.5 + 10.5·(m − 1) (→ 18 на 2 мес.)
 * 2–18 мес. (2 < m ≤ 18): ЧГ = 18 + 3·(m − 2)   (→ 66 на 18 мес.)
 * > 18 мес. (m > 18):   ЧГ = 66 + 4·(m − 18)
 * ```
 *
 * Формула едина для всех видов ([HamsterType] на возраст не влияет).
 */
class HamsterAgeCalculatorTest {
    private val calculator = HamsterAgeCalculator

    @Test
    fun `1 month hamster is about 7_5 human years`() {
        // m = 1 → 7.5 · 1 (≈ 1 ЧГ за 4 дня; §4.4 даёт ~7.6)
        val result = calculator.toHumanYears(ageInYears = 1.0 / 12.0, type = HamsterType.Syrian)
        assertEquals(7.5, result, 0.2)
    }

    @Test
    fun `2 months hamster equals 18 human years`() {
        val result = calculator.toHumanYears(ageInYears = 2.0 / 12.0, type = HamsterType.Syrian)
        assertEquals(18.0, result, 0.01)
    }

    @Test
    fun `6 months hamster equals 30 human years`() {
        val result = calculator.toHumanYears(ageInYears = 0.5, type = HamsterType.Syrian)
        assertEquals(30.0, result, 0.01)
    }

    @Test
    fun `12 months hamster equals 48 human years`() {
        val result = calculator.toHumanYears(ageInYears = 1.0, type = HamsterType.Syrian)
        assertEquals(48.0, result, 0.01)
    }

    @Test
    fun `18 months hamster equals 66 human years`() {
        // 30 + 3·12 = 66
        val result = calculator.toHumanYears(ageInYears = 1.5, type = HamsterType.Syrian)
        assertEquals(66.0, result, 0.01)
    }

    @Test
    fun `24 months hamster equals 90 human years`() {
        // 66 + 4·6 = 90
        val result = calculator.toHumanYears(ageInYears = 2.0, type = HamsterType.Syrian)
        assertEquals(90.0, result, 0.01)
    }

    @Test
    fun `type does not affect human years`() {
        val syrian = calculator.toHumanYears(1.0, HamsterType.Syrian)
        for (type in HamsterType.entries) {
            assertEquals(syrian, calculator.toHumanYears(1.0, type), 0.01)
        }
    }

    @Test
    fun `throws on zero age`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(0.0, HamsterType.Syrian)
        }
    }

    @Test
    fun `throws on negative age`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(-1.0, HamsterType.Syrian)
        }
    }

    @Test
    fun `unified toHumanYears via params matches type overload`() {
        val params = SpeciesParams.Hamster(type = HamsterType.Roborovski)
        val viaParams = calculator.toHumanYears(2.0, params)
        val viaOverload = calculator.toHumanYears(2.0, HamsterType.Roborovski)
        assertEquals(viaOverload, viaParams, 1e-9)
        assertEquals(90.0, viaParams, 0.01)
    }

    @Test
    fun `rejects non-Hamster params`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(1.0, SpeciesParams.Cat(app.pawclock.model.CatType.IndoorShortHair))
        }
    }

    @ParameterizedTest(name = "Hamster at {0} years ≈ {1} human years")
    @CsvSource(
        "0.16667, 18.0", // 2 месяца → граница второго/третьего сегмента
        "0.25, 21.0", // 3 месяца: 18 + 3·1
        "0.5, 30.0", // 6 месяцев: 18 + 3·4
        "0.75, 39.0", // 9 месяцев: 18 + 3·7
        "1.0, 48.0", // 12 месяцев: 18 + 3·10
        "1.25, 57.0", // 15 месяцев: 18 + 3·13
        "1.5, 66.0", // 18 месяцев → граница четвёртого сегмента
        "2.0, 90.0", // 24 месяца: 66 + 4·6
        "2.5, 114.0", // 30 месяцев: 66 + 4·12
    )
    fun `human years for table ages`(
        hamsterAge: Double,
        expectedHuman: Double,
    ) {
        val result = calculator.toHumanYears(hamsterAge, HamsterType.Syrian)
        assertEquals(expectedHuman, result, 0.05)
    }

    @Test
    fun `result is monotonically increasing in age`() {
        var previous = Double.NEGATIVE_INFINITY
        for (ageHundredths in 1..400) {
            val age = ageHundredths / 100.0
            val current = calculator.toHumanYears(age, HamsterType.Syrian)
            assert(current > previous) {
                "Non-monotonic at age=$age: previous=$previous, current=$current"
            }
            previous = current
        }
    }
}
