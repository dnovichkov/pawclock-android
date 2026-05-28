package app.pawclock.domain.format

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Тесты AgePluralFormatter — pure-Kotlin форматтера возраста с учётом CLDR-правил
 * множественного числа для русского и английского языков.
 *
 * Эталонные значения из спецификации §11.11.
 *
 * Русская сложная схема (CLDR rule):
 *   one: n mod 10 == 1 and n mod 100 != 11  → "год"      (1, 21, 31, ..., 101, ...)
 *   few: n mod 10 in 2..4 and n mod 100 not in 12..14 → "года" (2-4, 22-24, ...)
 *   many/other: иначе → "лет"  (0, 5-20, 25-30, ...)
 *
 * Английская схема:
 *   one: n == 1  → "year"
 *   other: n != 1 → "years"
 */
class AgePluralFormatterTest {
    private val formatter = AgePluralFormatter()

    @Test
    fun `formats 1 year in russian`() {
        assertEquals("1 год", formatter.formatYears(1, "ru"))
    }

    @Test
    fun `formats 2 years in russian as few`() {
        assertEquals("2 года", formatter.formatYears(2, "ru"))
    }

    @Test
    fun `formats 5 years in russian as many`() {
        assertEquals("5 лет", formatter.formatYears(5, "ru"))
    }

    @Test
    fun `formats 21 years in russian as one`() {
        assertEquals("21 год", formatter.formatYears(21, "ru"))
    }

    @Test
    fun `formats 22 years in russian as few`() {
        assertEquals("22 года", formatter.formatYears(22, "ru"))
    }

    @Test
    fun `formats 1 year in english`() {
        assertEquals("1 year", formatter.formatYears(1, "en"))
    }

    @Test
    fun `formats 2 years in english`() {
        assertEquals("2 years", formatter.formatYears(2, "en"))
    }

    @ParameterizedTest
    @CsvSource(
        // boundary cases для русского — все формы
        "0,    лет",
        "1,    год",
        "2,    года",
        "3,    года",
        "4,    года",
        "5,    лет",
        "10,   лет",
        "11,   лет",
        "12,   лет",
        "14,   лет",
        "15,   лет",
        "20,   лет",
        "21,   год",
        "22,   года",
        "25,   лет",
        "101,  год",
        "111,  лет",
        "112,  лет",
        "121,  год",
    )
    fun `russian plural form is selected per CLDR rules`(
        years: Int,
        expectedWord: String,
    ) {
        val result = formatter.formatYears(years, "ru")
        assertEquals("$years $expectedWord", result)
    }

    @ParameterizedTest
    @CsvSource(
        "0,   years",
        "1,   year",
        "2,   years",
        "5,   years",
        "21,  years",
        "100, years",
    )
    fun `english plural form follows one-or-other rule`(
        years: Int,
        expectedWord: String,
    ) {
        val result = formatter.formatYears(years, "en")
        assertEquals("$years $expectedWord", result)
    }

    @Test
    fun `unknown locale falls back to english`() {
        assertEquals("5 years", formatter.formatYears(5, "fr"))
        assertEquals("1 year", formatter.formatYears(1, "de"))
    }

    @Test
    fun `locale tag with region is normalized to language`() {
        assertEquals("5 лет", formatter.formatYears(5, "ru-RU"))
        assertEquals("5 лет", formatter.formatYears(5, "ru_RU"))
        assertEquals("5 years", formatter.formatYears(5, "en-US"))
    }

    @Test
    fun `negative years throws IllegalArgumentException`() {
        try {
            formatter.formatYears(-1, "ru")
            error("Expected IllegalArgumentException for negative years")
        } catch (expected: IllegalArgumentException) {
            // ok
        }
    }
}
