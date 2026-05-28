package app.pawclock.calculator

import app.pawclock.model.CatType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * TDD-тесты формулы AAHA / AAFP 2021 для кошек.
 *
 * Эталонные значения взяты из спецификации PawClock §4.2.
 *
 * Кусочная формула:
 * ```
 * ЧГ(0..1] = 15 · age
 * ЧГ(1..2] = 15 + 9 · (age − 1)            // от 15 ЧГ к 24 ЧГ
 * ЧГ(2..)  = 24 + 4 · (age − 2)            // далее +4 ЧГ/год
 * ```
 *
 * Поправки:
 *  - [CatType.Outdoor] после 2 лет → результат × 1.15
 *  - [CatType.LargeBreed] после 2 лет → результат + (age − 2)
 *
 * Источник: 2021 AAHA / AAFP Feline Life Stage Guidelines.
 * DOI: 10.1177/1098612X21993657
 */
class CatAgeCalculatorTest {
    private val calculator = CatAgeCalculator()

    @Test
    fun `1 year cat equals 15 human years`() {
        val result = calculator.toHumanYears(ageInYears = 1.0, catType = CatType.IndoorShortHair)
        assertEquals(15.0, result, 0.01)
    }

    @Test
    fun `2 year cat equals 24 human years`() {
        val result = calculator.toHumanYears(ageInYears = 2.0, catType = CatType.IndoorShortHair)
        assertEquals(24.0, result, 0.01)
    }

    @Test
    fun `5 year cat equals 36 human years`() {
        // 24 + 4 · (5 − 2) = 36
        val result = calculator.toHumanYears(ageInYears = 5.0, catType = CatType.IndoorShortHair)
        assertEquals(36.0, result, 0.01)
    }

    @Test
    fun `outdoor 5 year cat equals 41 dot 4 human years`() {
        // 36 · 1.15 = 41.4
        val result = calculator.toHumanYears(ageInYears = 5.0, catType = CatType.Outdoor)
        assertEquals(41.4, result, 0.01)
    }

    @Test
    fun `large breed 5 year cat equals 39 human years`() {
        // 36 + 1 · (5 − 2) = 39
        val result = calculator.toHumanYears(ageInYears = 5.0, catType = CatType.LargeBreed)
        assertEquals(39.0, result, 0.01)
    }

    @Test
    fun `throws on zero age`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(0.0, CatType.IndoorShortHair)
        }
    }

    @Test
    fun `throws on negative age`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(-1.0, CatType.IndoorShortHair)
        }
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(-0.5, CatType.IndoorShortHair)
        }
    }

    @Test
    fun `indoor long hair has same baseline as indoor short hair`() {
        // IndoorLongHair и IndoorShortHair одинаково обрабатываются формулой —
        // поправки применяются только для Outdoor и LargeBreed.
        val short = calculator.toHumanYears(5.0, CatType.IndoorShortHair)
        val long = calculator.toHumanYears(5.0, CatType.IndoorLongHair)
        assertEquals(short, long, 0.01)
    }

    @Test
    fun `outdoor correction does not apply before 2 years`() {
        // Outdoor-поправка ×1.15 действует только после 2 лет.
        // На age=1 значение должно совпадать с indoor.
        val indoor = calculator.toHumanYears(1.0, CatType.IndoorShortHair)
        val outdoor = calculator.toHumanYears(1.0, CatType.Outdoor)
        assertEquals(indoor, outdoor, 0.01)
    }

    @Test
    fun `large breed correction does not apply before 2 years`() {
        val indoor = calculator.toHumanYears(1.5, CatType.IndoorShortHair)
        val largeBreed = calculator.toHumanYears(1.5, CatType.LargeBreed)
        assertEquals(indoor, largeBreed, 0.01)
    }

    @Test
    fun `outdoor correction kicks in at exactly 2 years`() {
        // На границе age=2 поправка ещё не применяется (так как (age-2)=0):
        // 24 · 1.15 = 27.6 vs indoor 24 — формула «после 2» означает age > 2.
        // Реализация: применяем коэффициент при age > 2.
        val indoor2 = calculator.toHumanYears(2.0, CatType.IndoorShortHair)
        val outdoor2 = calculator.toHumanYears(2.0, CatType.Outdoor)
        assertEquals(indoor2, outdoor2, 0.01)
    }

    @ParameterizedTest(name = "Indoor cat at {0} years ≈ {1} human years")
    @CsvSource(
        "0.5, 7.5",
        "1.0, 15.0",
        "1.5, 19.5",
        "2.0, 24.0",
        "3.0, 28.0",
        "5.0, 36.0",
        "7.0, 44.0",
        "10.0, 56.0",
        "15.0, 76.0",
        "20.0, 96.0",
    )
    fun `indoor cat human years for table ages`(
        catAge: Double,
        expectedHuman: Double,
    ) {
        val result = calculator.toHumanYears(catAge, CatType.IndoorShortHair)
        assertEquals(expectedHuman, result, 0.01)
    }

    @ParameterizedTest(name = "Outdoor cat at {0} years ≈ {1} human years")
    @CsvSource(
        // Для age <= 2 поправка не применяется — значения как у indoor.
        "0.5, 7.5",
        "1.0, 15.0",
        "2.0, 24.0",
        // Для age > 2: indoor × 1.15
        "3.0, 32.2", // 28 · 1.15
        "5.0, 41.4", // 36 · 1.15
        "7.0, 50.6", // 44 · 1.15
    )
    fun `outdoor cat human years for table ages`(
        catAge: Double,
        expectedHuman: Double,
    ) {
        val result = calculator.toHumanYears(catAge, CatType.Outdoor)
        assertEquals(expectedHuman, result, 0.01)
    }

    @ParameterizedTest(name = "Large breed cat at {0} years ≈ {1} human years")
    @CsvSource(
        // Для age <= 2 поправка не применяется.
        "1.0, 15.0",
        "2.0, 24.0",
        // Для age > 2: indoor + (age − 2)
        "3.0, 29.0", // 28 + 1
        "5.0, 39.0", // 36 + 3
        "10.0, 64.0", // 56 + 8
    )
    fun `large breed cat human years for table ages`(
        catAge: Double,
        expectedHuman: Double,
    ) {
        val result = calculator.toHumanYears(catAge, CatType.LargeBreed)
        assertEquals(expectedHuman, result, 0.01)
    }

    @Test
    fun `result is monotonically increasing in age for indoor cat`() {
        var previous = Double.NEGATIVE_INFINITY
        for (ageHundredths in 1..2000) {
            val age = ageHundredths / 100.0
            val current = calculator.toHumanYears(age, CatType.IndoorShortHair)
            assert(current > previous) {
                "Non-monotonic at age=$age: previous=$previous, current=$current"
            }
            previous = current
        }
    }
}
