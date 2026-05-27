package app.pawclock.calculator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * TDD-тесты эпигенетической формулы Wang T. et al. 2020 для собак.
 *
 * Эталонные значения взяты из спецификации PawClock §4.1 и §11.5.
 * Формула: `ЧГ = 16 · ln(возраст_в_годах) + 31` (для возраст ≥ 1 года).
 * Источник: Wang T., Tsui B., Kreisberg J.F. et al. Cell Systems 2020;
 * DOI: 10.1016/j.cels.2020.06.006.
 */
class DogAgeCalculatorTest {
    private val calculator = DogAgeCalculator()

    @Test
    fun `Wang formula returns 31 human years for 1 year old dog`() {
        // 16 · ln(1) + 31 = 31
        val result =
            calculator.toHumanYears(
                ageInYears = 1.0,
                method = CalculationMethod.EPIGENETIC,
            )
        assertEquals(31.0, result, 0.1)
    }

    @ParameterizedTest(name = "Wang({0} years) ≈ {1} human years")
    @CsvSource(
        "1.0, 31.0",
        "2.0, 42.1",
        "5.0, 56.7",
        "10.0, 67.8",
        "12.0, 70.7",
    )
    fun `Wang formula returns expected human years for table values`(
        dogAge: Double,
        expectedHuman: Double,
    ) {
        val result = calculator.toHumanYears(dogAge, CalculationMethod.EPIGENETIC)
        assertEquals(expectedHuman, result, 0.2)
    }

    @Test
    fun `throws on zero age`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(0.0, CalculationMethod.EPIGENETIC)
        }
    }

    @Test
    fun `throws on negative age`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(-1.0, CalculationMethod.EPIGENETIC)
        }
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(-0.5, CalculationMethod.EPIGENETIC)
        }
    }

    @Test
    fun `handles 7 weeks old puppy via piecewise extension`() {
        // Wang формула определена для age >= 1 года.
        // Для возрастов < 1 года используем кусочное расширение:
        // human(age) = 31 · age^0.6 (степенная интерполяция, непрерывна в age=1).
        val sevenWeeks = 7.0 / 52.0
        val result = calculator.toHumanYears(sevenWeeks, CalculationMethod.EPIGENETIC)
        assertTrue(
            result in 8.0..10.0,
            "7-week-old puppy should map to ~9 human years, got $result",
        )
    }

    @Test
    fun `puppy extension is continuous with Wang formula at age 1`() {
        // Проверяем непрерывность в точке age=1: оба ветви должны давать 31.0.
        val justUnderOne = calculator.toHumanYears(0.9999, CalculationMethod.EPIGENETIC)
        val one = calculator.toHumanYears(1.0, CalculationMethod.EPIGENETIC)
        assertEquals(one, justUnderOne, 0.05)
    }

    @Test
    fun `6 months puppy is between 15 and 25 human years`() {
        // 6 месяцев = пубертат собаки. Биологически ~15-20 ЧГ (AAHA puppy guidance).
        val sixMonths = 0.5
        val result = calculator.toHumanYears(sixMonths, CalculationMethod.EPIGENETIC)
        assertTrue(
            result in 15.0..25.0,
            "6-month-old puppy should map to ~20 human years, got $result",
        )
    }
}
