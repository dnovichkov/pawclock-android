package app.pawclock.calculator

import app.pawclock.model.LifeStage
import kotlin.test.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Тесты [MouseLifeStageCalculator] — стадии жизни мыши по возрасту (Dutta & Sengupta 2016, §4.6).
 *
 * Пороги (единые — у мыши нет подкатегорий; «senior с 12 мес.»):
 *  - Pup: 0 < age < 0.058 (0–~3 нед.)
 *  - Juvenile: 0.058 ≤ age < 0.25 (~3 нед.–3 мес.)
 *  - Adult: 0.25 ≤ age < 1.0 (3 мес.–12 мес.)
 *  - Senior: 1.0 ≤ age < 2.0 (12–24 мес.)
 *  - EndOfLife: age ≥ 2.0
 */
class MouseLifeStageCalculatorTest {
    private val calculator = MouseLifeStageCalculator

    @Test
    fun `2 weeks mouse is Pup`() {
        val stage = calculator.determine(ageInYears = 14.0 / 365.0)
        assertEquals(LifeStage.Mouse.Pup, stage)
    }

    @Test
    fun `2 months mouse is Juvenile`() {
        val stage = calculator.determine(ageInYears = 2.0 / 12.0)
        assertEquals(LifeStage.Mouse.Juvenile, stage)
    }

    @Test
    fun `6 months mouse is Adult`() {
        val stage = calculator.determine(ageInYears = 0.5)
        assertEquals(LifeStage.Mouse.Adult, stage)
    }

    @Test
    fun `12 months mouse is Senior`() {
        val stage = calculator.determine(ageInYears = 1.0)
        assertEquals(LifeStage.Mouse.Senior, stage)
    }

    @Test
    fun `2 year mouse is EndOfLife`() {
        val stage = calculator.determine(ageInYears = 2.0)
        assertEquals(LifeStage.Mouse.EndOfLife, stage)
    }

    @ParameterizedTest(name = "{0} y -> {1}")
    @CsvSource(
        "0.01, Pup",
        "0.057, Pup",
        "0.058, Juvenile",
        "0.24, Juvenile",
        "0.25, Adult",
        "0.99, Adult",
        "1.0, Senior",
        "1.99, Senior",
        "2.0, EndOfLife",
        "2.5, EndOfLife",
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
        val viaParams = calculator.determine(1.0, SpeciesParams.Mouse)
        val viaOverload = calculator.determine(1.0)
        assertEquals(viaOverload, viaParams)
        assertEquals(LifeStage.Mouse.Senior, viaParams)
    }

    @Test
    fun `rejects non-Mouse params`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(1.0, SpeciesParams.Rat)
        }
    }

    @Test
    fun `stage ordinal is non-decreasing in age`() {
        var previousOrdinal = -1
        var age = 0.01
        while (age <= 3.0) {
            val stage = calculator.determine(ageInYears = age)
            assertTrue(
                stage.ordinal >= previousOrdinal,
                "age=$age: stage ordinal decreased from $previousOrdinal to ${stage.ordinal}",
            )
            previousOrdinal = stage.ordinal
            age += 0.01
        }
    }

    @Test
    fun `expectedLifespanRange returns 1 to 3 years`() {
        val range = calculator.expectedLifespanRange()
        assertEquals(1.0, range.start, 0.001)
        assertEquals(3.0, range.endInclusive, 0.001)
    }
}
