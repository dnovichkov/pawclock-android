package app.pawclock.calculator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * TDD-тесты кусочной модели Dutta & Sengupta 2016 для мышей.
 *
 * Возраст переводится в дни (× 365), затем по фазам накапливаются эквивалентные
 * человеческие дни (интеграл скорости старения), и сумма делится на 365.
 *
 * | Фаза (мышиные дни) | Скорость (человеко-дней / день) |
 * |--------------------|---------------------------------|
 * | 0 – 42             | 150                             |
 * | 42 – 180           | 45                              |
 * | 180 – 365          | 30                              |
 * | 365 – 730          | 25                              |
 * | > 730              | 20                              |
 *
 * Единственная математически выведенная опорная точка §4.6 — **30 дней = 30 · 150 / 365 ≈
 * 12.33 ЧГ**. Остальные эталонные значения §4.6 для бóльших возрастов внутренне противоречивы
 * (см. KDoc [MouseAgeCalculator] и план Plan 2, Task 5 ⚠️), поэтому здесь используются
 * значения, согласованные с кусочной интеграцией:
 *  - 30 дней (0.0822 г.) = 4500 чел.-дней / 365 ≈ 12.33 ЧГ
 *  - 100 дней = (42·150 + 58·45) / 365 = 8910 / 365 ≈ 24.41 ЧГ
 *  - 200 дней = (6300 + 6210 + 600) / 365 = 13110 / 365 ≈ 35.92 ЧГ
 *  - 500 дней = (6300 + 6210 + 5550 + 3375) / 365 = 21435 / 365 ≈ 58.73 ЧГ
 *  - 800 дней = (6300 + 6210 + 5550 + 9125 + 1400) / 365 = 28585 / 365 ≈ 78.32 ЧГ
 *
 * Формула едина для всех — у мыши нет подкатегорий.
 */
class MouseAgeCalculatorTest {
    private val calculator = MouseAgeCalculator

    private fun ageOfDays(days: Double): Double = days / 365.0

    @Test
    fun `30 days mouse equals about 12_33 human years`() {
        val result = calculator.toHumanYears(ageInYears = ageOfDays(30.0))
        assertEquals(12.33, result, 0.01)
    }

    @Test
    fun `100 days mouse equals about 24_41 human years`() {
        val result = calculator.toHumanYears(ageInYears = ageOfDays(100.0))
        assertEquals(24.41, result, 0.01)
    }

    @Test
    fun `200 days mouse equals about 35_92 human years`() {
        val result = calculator.toHumanYears(ageInYears = ageOfDays(200.0))
        assertEquals(35.92, result, 0.01)
    }

    @Test
    fun `500 days mouse equals about 58_73 human years`() {
        val result = calculator.toHumanYears(ageInYears = ageOfDays(500.0))
        assertEquals(58.73, result, 0.01)
    }

    @Test
    fun `800 days mouse equals about 78_32 human years`() {
        val result = calculator.toHumanYears(ageInYears = ageOfDays(800.0))
        assertEquals(78.32, result, 0.01)
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
        val age = ageOfDays(200.0)
        val viaParams = calculator.toHumanYears(age, SpeciesParams.Mouse)
        val viaOverload = calculator.toHumanYears(age)
        assertEquals(viaOverload, viaParams, 1e-9)
        assertEquals(35.92, viaParams, 0.01)
    }

    @Test
    fun `rejects non-Mouse params`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(0.5, SpeciesParams.Rat)
        }
    }

    @ParameterizedTest(name = "Mouse at {0} days ≈ {1} human years")
    @CsvSource(
        "30.0, 12.33", // фаза 1
        "42.0, 17.26", // граница фаз 1/2: 6300/365
        "100.0, 24.41", // фаза 2
        "180.0, 34.27", // граница фаз 2/3: 12510/365
        "200.0, 35.92", // фаза 3
        "365.0, 49.48", // граница фаз 3/4 (1 год): 18060/365
        "500.0, 58.73", // фаза 4
        "730.0, 74.48", // граница фаз 4/5 (2 года): 27185/365
        "800.0, 78.32", // фаза 5
    )
    fun `human years for table ages in days`(
        days: Double,
        expectedHuman: Double,
    ) {
        val result = calculator.toHumanYears(ageOfDays(days))
        assertEquals(expectedHuman, result, 0.01)
    }

    @Test
    fun `result is monotonically increasing in age`() {
        var previous = Double.NEGATIVE_INFINITY
        for (dayTenths in 1..10000) {
            val age = ageOfDays(dayTenths / 10.0)
            val current = calculator.toHumanYears(age)
            assert(current > previous) {
                "Non-monotonic at age=$age: previous=$previous, current=$current"
            }
            previous = current
        }
    }
}
