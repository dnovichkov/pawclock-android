package app.pawclock.calculator

import app.pawclock.model.LifeStage
import kotlin.test.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Тесты [GuineaPigLifeStageCalculator] — стадии жизни морской свинки по возрасту
 * (Oxbow + Animallama, §4.5 спецификации).
 *
 * Пороги (единые — у морской свинки нет подкатегорий):
 *  - Pup: 0 < age < 0.058 (0–~3 нед.)
 *  - Juvenile: 0.058 ≤ age < 0.42 (~3 нед.–5 мес.)
 *  - Adult: 0.42 ≤ age < 4.0 (5 мес.–4 года)
 *  - Senior: 4.0 ≤ age < 6.0
 *  - Geriatric: age ≥ 6.0
 */
class GuineaPigLifeStageCalculatorTest {
    private val calculator = GuineaPigLifeStageCalculator

    @Test
    fun `2 weeks guinea pig is Pup`() {
        val stage = calculator.determine(ageInYears = 14.0 / 365.0)
        assertEquals(LifeStage.GuineaPig.Pup, stage)
    }

    @Test
    fun `2 months guinea pig is Juvenile`() {
        val stage = calculator.determine(ageInYears = 2.0 / 12.0)
        assertEquals(LifeStage.GuineaPig.Juvenile, stage)
    }

    @Test
    fun `1 year guinea pig is Adult`() {
        val stage = calculator.determine(ageInYears = 1.0)
        assertEquals(LifeStage.GuineaPig.Adult, stage)
    }

    @Test
    fun `5 year guinea pig is Senior`() {
        val stage = calculator.determine(ageInYears = 5.0)
        assertEquals(LifeStage.GuineaPig.Senior, stage)
    }

    @Test
    fun `7 year guinea pig is Geriatric`() {
        val stage = calculator.determine(ageInYears = 7.0)
        assertEquals(LifeStage.GuineaPig.Geriatric, stage)
    }

    @ParameterizedTest(name = "{0} y -> {1}")
    @CsvSource(
        "0.01, Pup",
        "0.057, Pup",
        "0.058, Juvenile",
        "0.41, Juvenile",
        "0.42, Adult",
        "3.99, Adult",
        "4.0, Senior",
        "5.99, Senior",
        "6.0, Geriatric",
        "9.0, Geriatric",
    )
    fun `determine returns expected stage at boundaries`(
        ageInYears: Double,
        expectedStageName: String,
    ) {
        val stage = calculator.determine(ageInYears = ageInYears)
        assertEquals(expectedStageName, stage::class.simpleName)
    }

    @Test
    fun `determine throws on zero age`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(ageInYears = 0.0)
        }
    }

    @Test
    fun `determine throws on negative age`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(ageInYears = -1.0)
        }
    }

    @Test
    fun `unified determine via params matches direct overload`() {
        val viaParams = calculator.determine(5.0, SpeciesParams.GuineaPig)
        val viaOverload = calculator.determine(5.0)
        assertEquals(viaOverload, viaParams)
        assertEquals(LifeStage.GuineaPig.Senior, viaParams)
    }

    @Test
    fun `rejects non-GuineaPig params`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(5.0, SpeciesParams.Cat(app.pawclock.model.CatType.IndoorShortHair))
        }
    }

    @Test
    fun `stage ordinal is non-decreasing in age`() {
        var previousOrdinal = -1
        var age = 0.01
        while (age <= 9.0) {
            val stage = calculator.determine(ageInYears = age)
            assertTrue(
                stage.ordinal >= previousOrdinal,
                "age=$age: stage ordinal decreased from $previousOrdinal to ${stage.ordinal}",
            )
            previousOrdinal = stage.ordinal
            age += 0.01
        }
    }

    // ===== expectedLifespanRange =====

    @Test
    fun `expectedLifespanRange returns 5 to 7 years`() {
        val range = calculator.expectedLifespanRange()
        assertEquals(5.0, range.start, 0.001)
        assertEquals(7.0, range.endInclusive, 0.001)
    }
}
