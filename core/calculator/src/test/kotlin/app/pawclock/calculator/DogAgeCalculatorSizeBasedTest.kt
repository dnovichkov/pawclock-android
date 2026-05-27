package app.pawclock.calculator

import app.pawclock.model.DogSize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * TDD-тесты табличного метода AKC / AAHA 2019 для собак.
 *
 * Эталонные значения взяты напрямую из таблицы спецификации PawClock §4.1:
 *
 * | Возраст | Малая ≤9 кг | Средняя 9–23 | Крупная 23–45 | Гигантская >45 |
 * |---------|-------------|--------------|---------------|----------------|
 * |   1     |     15      |      15      |      15       |       12       |
 * |   2     |     24      |      24      |      24       |       22       |
 * |   3     |     28      |      28      |      28       |       31       |
 * |   4     |     32      |      32      |      32       |       38       |
 * |   5     |     36      |      36      |      36       |       45       |
 * |   6     |     40      |      42      |      45       |       49       |
 * |   7     |     44      |      47      |      50       |       56       |
 * |   8     |     48      |      51      |      55       |       64       |
 * |  10     |     56      |      60      |      66       |       79       |
 * |  12     |     64      |      69      |      77       |       93       |
 * |  14     |     72      |      78      |      88       |      107       |
 * |  15     |     76      |      83      |      93       |      114       |
 *
 * В DogSize колонка "Малая ≤9 кг" соответствует и [DogSize.Toy], и [DogSize.Small]
 * (обе подгруппы используют одну и ту же шкалу старения по AKC/AAHA 2019).
 */
class DogAgeCalculatorSizeBasedTest {
    private val calculator = DogAgeCalculator()

    @ParameterizedTest(name = "AKC table: Toy({0}y) = {1}")
    @CsvSource(
        "1.0, 15.0",
        "2.0, 24.0",
        "3.0, 28.0",
        "4.0, 32.0",
        "5.0, 36.0",
        "6.0, 40.0",
        "7.0, 44.0",
        "8.0, 48.0",
        "10.0, 56.0",
        "12.0, 64.0",
        "14.0, 72.0",
        "15.0, 76.0",
    )
    fun `Toy size matches AKC table values`(
        dogAge: Double,
        expectedHuman: Double,
    ) {
        val result = calculator.toHumanYears(dogAge, DogSize.Toy)
        assertEquals(expectedHuman, result, 0.01)
    }

    @ParameterizedTest(name = "AKC table: Small({0}y) = {1}")
    @CsvSource(
        "1.0, 15.0",
        "5.0, 36.0",
        "10.0, 56.0",
        "15.0, 76.0",
    )
    fun `Small size matches AKC table values`(
        dogAge: Double,
        expectedHuman: Double,
    ) {
        val result = calculator.toHumanYears(dogAge, DogSize.Small)
        assertEquals(expectedHuman, result, 0.01)
    }

    @ParameterizedTest(name = "AKC table: Medium({0}y) = {1}")
    @CsvSource(
        "1.0, 15.0",
        "2.0, 24.0",
        "5.0, 36.0",
        "6.0, 42.0",
        "8.0, 51.0",
        "10.0, 60.0",
        "12.0, 69.0",
        "15.0, 83.0",
    )
    fun `Medium size matches AKC table values`(
        dogAge: Double,
        expectedHuman: Double,
    ) {
        val result = calculator.toHumanYears(dogAge, DogSize.Medium)
        assertEquals(expectedHuman, result, 0.01)
    }

    @ParameterizedTest(name = "AKC table: Large({0}y) = {1}")
    @CsvSource(
        "1.0, 15.0",
        "5.0, 36.0",
        "6.0, 45.0",
        "8.0, 55.0",
        "10.0, 66.0",
        "15.0, 93.0",
    )
    fun `Large size matches AKC table values`(
        dogAge: Double,
        expectedHuman: Double,
    ) {
        val result = calculator.toHumanYears(dogAge, DogSize.Large)
        assertEquals(expectedHuman, result, 0.01)
    }

    @ParameterizedTest(name = "AKC table: Giant({0}y) = {1}")
    @CsvSource(
        "1.0, 12.0",
        "2.0, 22.0",
        "3.0, 31.0",
        "5.0, 45.0",
        "6.0, 49.0",
        "8.0, 64.0",
        "10.0, 79.0",
        "15.0, 114.0",
    )
    fun `Giant size matches AKC table values`(
        dogAge: Double,
        expectedHuman: Double,
    ) {
        val result = calculator.toHumanYears(dogAge, DogSize.Giant)
        assertEquals(expectedHuman, result, 0.01)
    }

    @Test
    fun `linear interpolation between table values for half-integer age`() {
        // Toy 3y = 28, Toy 4y = 32, поэтому Toy 3.5y = (28+32)/2 = 30
        val result = calculator.toHumanYears(3.5, DogSize.Toy)
        assertEquals(30.0, result, 0.01)
    }

    @Test
    fun `linear interpolation across gap between 8 and 10 years`() {
        // Medium 8y = 51, Medium 10y = 60, поэтому Medium 9y = (51+60)/2 = 55.5
        val result = calculator.toHumanYears(9.0, DogSize.Medium)
        assertEquals(55.5, result, 0.01)
    }

    @Test
    fun `linear interpolation respects gap-weighted ages`() {
        // Giant 12y = 93, Giant 14y = 107.
        // Giant 13y = (93 + 107) / 2 = 100
        val result = calculator.toHumanYears(13.0, DogSize.Giant)
        assertEquals(100.0, result, 0.01)
    }

    @Test
    fun `puppy under one year uses scaled extrapolation`() {
        // Для age < 1 года используем линейную интерполяцию между 0 ЧГ (age=0) и
        // первой табличной точкой (1y = 15 для Toy/Small/Medium/Large; 12 для Giant).
        val sixMonthsToy = calculator.toHumanYears(0.5, DogSize.Toy)
        assertEquals(7.5, sixMonthsToy, 0.01)
        val sixMonthsGiant = calculator.toHumanYears(0.5, DogSize.Giant)
        assertEquals(6.0, sixMonthsGiant, 0.01)
    }

    @Test
    fun `age above table is extrapolated using last slope`() {
        // Toy: 14y = 72, 15y = 76 → шаг 4 ЧГ/год; 20y = 76 + 5 * 4 = 96.
        val result = calculator.toHumanYears(20.0, DogSize.Toy)
        assertTrue(
            result in 90.0..100.0,
            "20y Toy should extrapolate to ~96 human years, got $result",
        )
    }

    @Test
    fun `Giant extrapolated to 20 years gives sensible value`() {
        // Giant: 14y = 107, 15y = 114 → шаг 7 ЧГ/год; 20y = 114 + 5 * 7 = 149.
        val result = calculator.toHumanYears(20.0, DogSize.Giant)
        assertTrue(
            result in 140.0..160.0,
            "20y Giant should extrapolate to ~149 human years, got $result",
        )
    }

    @Test
    fun `throws on zero age`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(0.0, DogSize.Toy)
        }
    }

    @Test
    fun `throws on negative age`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(-1.0, DogSize.Medium)
        }
    }

    @Test
    fun `size-based via method enum delegates to size-based implementation`() {
        // Перегрузка через CalculationMethod.SIZE_BASED + DogSize должна давать тот же результат.
        val viaSize = calculator.toHumanYears(5.0, DogSize.Medium)
        val viaMethod =
            calculator.toHumanYears(
                ageInYears = 5.0,
                method = CalculationMethod.SIZE_BASED,
                size = DogSize.Medium,
            )
        assertEquals(viaSize, viaMethod, 0.01)
    }

    @Test
    fun `giant ages faster than toy after 5 years`() {
        // По AKC/AAHA: гигантские стареют быстрее малых начиная с 3 лет (когда их кривая обгоняет).
        // Toy 8y = 48, Giant 8y = 64.
        val toyEight = calculator.toHumanYears(8.0, DogSize.Toy)
        val giantEight = calculator.toHumanYears(8.0, DogSize.Giant)
        assertTrue(
            giantEight > toyEight,
            "Giant ($giantEight) should exceed Toy ($toyEight) at age 8",
        )
    }
}
