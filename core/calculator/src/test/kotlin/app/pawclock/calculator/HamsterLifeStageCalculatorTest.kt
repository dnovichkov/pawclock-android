package app.pawclock.calculator

import app.pawclock.model.HamsterType
import app.pawclock.model.LifeStage
import kotlin.test.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Тесты [HamsterLifeStageCalculator] — стадии жизни хомяка по возрасту
 * (RVC VetCompass + Animallama, §4.4 спецификации).
 *
 * Пороги (единые для всех видов):
 *  - Pup: 0 < age < 0.0575 (0–~3 нед.)
 *  - Juvenile: 0.0575 ≤ age < 0.1667 (~3 нед.–2 мес.)
 *  - Adult: 0.1667 ≤ age < 1.0 (2 мес.–1 год)
 *  - Senior: 1.0 ≤ age < 1.5
 *  - VerySenior: age ≥ 1.5
 */
class HamsterLifeStageCalculatorTest {
    private val calculator = HamsterLifeStageCalculator

    @Test
    fun `2 weeks hamster is Pup`() {
        val stage = calculator.determine(ageInYears = 14.0 / 365.0, type = HamsterType.Syrian)
        assertEquals(LifeStage.Hamster.Pup, stage)
    }

    @Test
    fun `1 month hamster is Juvenile`() {
        val stage = calculator.determine(ageInYears = 1.0 / 12.0, type = HamsterType.Syrian)
        assertEquals(LifeStage.Hamster.Juvenile, stage)
    }

    @Test
    fun `6 months hamster is Adult`() {
        val stage = calculator.determine(ageInYears = 0.5, type = HamsterType.Syrian)
        assertEquals(LifeStage.Hamster.Adult, stage)
    }

    @Test
    fun `13 months hamster is Senior`() {
        val stage = calculator.determine(ageInYears = 13.0 / 12.0, type = HamsterType.Syrian)
        assertEquals(LifeStage.Hamster.Senior, stage)
    }

    @Test
    fun `2 year hamster is VerySenior`() {
        val stage = calculator.determine(ageInYears = 2.0, type = HamsterType.Syrian)
        assertEquals(LifeStage.Hamster.VerySenior, stage)
    }

    @ParameterizedTest(name = "{0} y -> {1}")
    @CsvSource(
        "0.01, Pup",
        "0.057, Pup",
        "0.0575, Juvenile",
        "0.16, Juvenile",
        "0.16667, Adult",
        "0.99, Adult",
        "1.0, Senior",
        "1.49, Senior",
        "1.5, VerySenior",
        "3.0, VerySenior",
    )
    fun `determine returns expected stage at boundaries`(
        ageInYears: Double,
        expectedStageName: String,
    ) {
        val stage = calculator.determine(ageInYears = ageInYears, type = HamsterType.Syrian)
        assertEquals(expectedStageName, stage::class.simpleName)
    }

    @Test
    fun `determine throws on zero age`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(ageInYears = 0.0, type = HamsterType.Syrian)
        }
    }

    @Test
    fun `determine throws on negative age`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(ageInYears = -1.0, type = HamsterType.Syrian)
        }
    }

    @Test
    fun `unified determine via params matches type overload`() {
        val params = SpeciesParams.Hamster(type = HamsterType.Dwarf)
        val viaParams = calculator.determine(2.0, params)
        val viaOverload = calculator.determine(2.0, HamsterType.Dwarf)
        assertEquals(viaOverload, viaParams)
        assertEquals(LifeStage.Hamster.VerySenior, viaParams)
    }

    @Test
    fun `rejects non-Hamster params`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(2.0, SpeciesParams.Cat(app.pawclock.model.CatType.IndoorShortHair))
        }
    }

    @Test
    fun `stage ordinal is non-decreasing in age`() {
        var previousOrdinal = -1
        var age = 0.01
        while (age <= 4.0) {
            val stage = calculator.determine(ageInYears = age, type = HamsterType.Syrian)
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
    fun `expectedLifespanRange Syrian returns 2 to 3 years`() {
        val range = calculator.expectedLifespanRange(HamsterType.Syrian)
        assertEquals(2.0, range.start, 0.001)
        assertEquals(3.0, range.endInclusive, 0.001)
    }

    @Test
    fun `expectedLifespanRange Roborovski lives longer than Dwarf`() {
        val robo = calculator.expectedLifespanRange(HamsterType.Roborovski)
        val dwarf = calculator.expectedLifespanRange(HamsterType.Dwarf)
        assertTrue(
            robo.endInclusive > dwarf.endInclusive,
            "Roborovski hamsters should outlive dwarf hamsters",
        )
    }

    @Test
    fun `expectedLifespanRange Dwarf returns 1_5 to 2 years`() {
        val range = calculator.expectedLifespanRange(HamsterType.Dwarf)
        assertEquals(1.5, range.start, 0.001)
        assertEquals(2.0, range.endInclusive, 0.001)
    }
}
