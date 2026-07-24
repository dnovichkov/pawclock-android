package app.pawclock.calculator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * TDD-тесты кусочной формулы Oxbow + Animallama для морских свинок.
 *
 * Эталонные значения из спецификации PawClock §4.5 (и плана Plan 2, Task 4):
 *  - 3 нед. (0.058 г.) ≈ 6 ЧГ (отъём)
 *  - 2.5 мес. (0.208 г.) ≈ 11.5 ЧГ (половая зрелость)
 *  - 5 мес. (0.42 г.) ≈ 20 ЧГ
 *  - 1 год ≈ 24.6 ЧГ (после 5 мес. +8/год)
 *  - 4 года ≈ 48.6 ЧГ
 *  - 7 лет ≈ 78.6 ЧГ (после 4 лет +10/год)
 *
 * Кусочная формула (age в годах) непрерывна на всех стыках:
 * ```
 * 0–3 нед. (age ≤ 0.058):    линейно 0 → 6
 * 3 нед.–2.5 мес. (≤ 0.208): линейно 6 → 11.5
 * 2.5–5 мес. (≤ 0.42):       линейно 11.5 → 20
 * 5 мес.–4 года (≤ 4.0):     20 + 8 · (age − 0.42)
 * > 4 года:                  48.64 + 10 · (age − 4)
 * ```
 *
 * Формула едина для всех — у морской свинки нет подкатегорий.
 */
class GuineaPigAgeCalculatorTest {
    private val calculator = GuineaPigAgeCalculator

    @Test
    fun `3 weeks guinea pig equals about 6 human years at weaning`() {
        val result = calculator.toHumanYears(ageInYears = 0.058)
        assertEquals(6.0, result, 0.01)
    }

    @Test
    fun `2_5 months guinea pig equals about 11_5 human years at puberty`() {
        val result = calculator.toHumanYears(ageInYears = 0.208)
        assertEquals(11.5, result, 0.05)
    }

    @Test
    fun `5 months guinea pig equals 20 human years at maturity`() {
        val result = calculator.toHumanYears(ageInYears = 0.42)
        assertEquals(20.0, result, 0.01)
    }

    @Test
    fun `1 year guinea pig equals about 24_6 human years`() {
        // 20 + 8 · (1 − 0.42) = 24.64
        val result = calculator.toHumanYears(ageInYears = 1.0)
        assertEquals(24.64, result, 0.01)
    }

    @Test
    fun `4 year guinea pig equals about 48_6 human years at senior boundary`() {
        // 20 + 8 · (4 − 0.42) = 48.64
        val result = calculator.toHumanYears(ageInYears = 4.0)
        assertEquals(48.64, result, 0.01)
    }

    @Test
    fun `7 year guinea pig equals about 78_6 human years`() {
        // 48.64 + 10 · (7 − 4) = 78.64
        val result = calculator.toHumanYears(ageInYears = 7.0)
        assertEquals(78.64, result, 0.01)
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
        val viaParams = calculator.toHumanYears(7.0, SpeciesParams.GuineaPig)
        val viaOverload = calculator.toHumanYears(7.0)
        assertEquals(viaOverload, viaParams, 1e-9)
        assertEquals(78.64, viaParams, 0.01)
    }

    @Test
    fun `rejects non-GuineaPig params`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(5.0, SpeciesParams.Cat(app.pawclock.model.CatType.IndoorShortHair))
        }
    }

    @ParameterizedTest(name = "GuineaPig at {0} years ≈ {1} human years")
    @CsvSource(
        "0.029, 3.0", // половина отъёма: линейный сегмент 0 → 6
        "0.058, 6.0", // отъём
        "0.208, 11.5", // половая зрелость
        "0.42, 20.0", // физическая зрелость
        "1.0, 24.64", // 20 + 8 · 0.58
        "2.0, 32.64", // 20 + 8 · 1.58
        "4.0, 48.64", // граница senior
        "5.0, 58.64", // 48.64 + 10 · 1
        "7.0, 78.64", // 48.64 + 10 · 3
    )
    fun `human years for table ages`(
        guineaPigAge: Double,
        expectedHuman: Double,
    ) {
        val result = calculator.toHumanYears(guineaPigAge)
        assertEquals(expectedHuman, result, 0.05)
    }

    @Test
    fun `result is monotonically increasing in age`() {
        var previous = Double.NEGATIVE_INFINITY
        for (ageHundredths in 1..800) {
            val age = ageHundredths / 100.0
            val current = calculator.toHumanYears(age)
            assert(current > previous) {
                "Non-monotonic at age=$age: previous=$previous, current=$current"
            }
            previous = current
        }
    }
}
