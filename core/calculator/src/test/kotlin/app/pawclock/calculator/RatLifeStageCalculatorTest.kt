package app.pawclock.calculator

import app.pawclock.model.LifeStage
import kotlin.test.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Тесты [RatLifeStageCalculator] — стадии жизни крысы по возрасту (Sengupta 2013, §4.6).
 *
 * Пороги (единые — у крысы нет подкатегорий; «senior с 18 мес.»):
 *  - Pup: 0 < age < 0.058 (0–~3 нед.)
 *  - Juvenile: 0.058 ≤ age < 0.25 (~3 нед.–3 мес.)
 *  - Adult: 0.25 ≤ age < 1.5 (3 мес.–18 мес.)
 *  - Senior: 1.5 ≤ age < 2.5 (18–30 мес.)
 *  - EndOfLife: age ≥ 2.5
 */
class RatLifeStageCalculatorTest {
    private val calculator = RatLifeStageCalculator

    @Test
    fun `2 weeks rat is Pup`() {
        val stage = calculator.determine(ageInYears = 14.0 / 365.0)
        assertEquals(LifeStage.Rat.Pup, stage)
    }

    @Test
    fun `2 months rat is Juvenile`() {
        val stage = calculator.determine(ageInYears = 2.0 / 12.0)
        assertEquals(LifeStage.Rat.Juvenile, stage)
    }

    @Test
    fun `1 year rat is Adult`() {
        val stage = calculator.determine(ageInYears = 1.0)
        assertEquals(LifeStage.Rat.Adult, stage)
    }

    @Test
    fun `18 months rat is Senior`() {
        val stage = calculator.determine(ageInYears = 1.5)
        assertEquals(LifeStage.Rat.Senior, stage)
    }

    @Test
    fun `3 year rat is EndOfLife`() {
        val stage = calculator.determine(ageInYears = 3.0)
        assertEquals(LifeStage.Rat.EndOfLife, stage)
    }

    @ParameterizedTest(name = "{0} y -> {1}")
    @CsvSource(
        "0.01, Pup",
        "0.057, Pup",
        "0.058, Juvenile",
        "0.24, Juvenile",
        "0.25, Adult",
        "1.49, Adult",
        "1.5, Senior",
        "2.49, Senior",
        "2.5, EndOfLife",
        "3.0, EndOfLife",
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
        val viaParams = calculator.determine(1.5, SpeciesParams.Rat)
        val viaOverload = calculator.determine(1.5)
        assertEquals(viaOverload, viaParams)
        assertEquals(LifeStage.Rat.Senior, viaParams)
    }

    @Test
    fun `rejects non-Rat params`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(1.5, SpeciesParams.Mouse)
        }
    }

    @Test
    fun `stage ordinal is non-decreasing in age`() {
        var previousOrdinal = -1
        var age = 0.01
        while (age <= 4.0) {
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
    fun `expectedLifespanRange returns 2 to 3 years`() {
        val range = calculator.expectedLifespanRange()
        assertEquals(2.0, range.start, 0.001)
        assertEquals(3.0, range.endInclusive, 0.001)
    }
}
