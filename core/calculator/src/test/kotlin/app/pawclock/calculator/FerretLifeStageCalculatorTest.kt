package app.pawclock.calculator

import app.pawclock.model.LifeStage
import kotlin.test.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Тесты [FerretLifeStageCalculator] — стадии жизни хорька по возрасту (PMC «Senior Ferret», §4.7).
 *
 * Пороги (единые — у хорька нет подкатегорий; «senior с 3–4 лет»):
 *  - Kit: 0 < age < 0.33 (0–~4 мес.)
 *  - Juvenile: 0.33 ≤ age < 1.0 (~4 мес.–1 год)
 *  - Adult: 1.0 ≤ age < 3.0
 *  - Senior: 3.0 ≤ age < 5.0
 *  - Geriatric: age ≥ 5.0
 */
class FerretLifeStageCalculatorTest {
    private val calculator = FerretLifeStageCalculator

    @Test
    fun `2 months ferret is Kit`() {
        val stage = calculator.determine(ageInYears = 2.0 / 12.0)
        assertEquals(LifeStage.Ferret.Kit, stage)
    }

    @Test
    fun `6 months ferret is Juvenile`() {
        val stage = calculator.determine(ageInYears = 0.5)
        assertEquals(LifeStage.Ferret.Juvenile, stage)
    }

    @Test
    fun `2 years ferret is Adult`() {
        val stage = calculator.determine(ageInYears = 2.0)
        assertEquals(LifeStage.Ferret.Adult, stage)
    }

    @Test
    fun `4 years ferret is Senior`() {
        val stage = calculator.determine(ageInYears = 4.0)
        assertEquals(LifeStage.Ferret.Senior, stage)
    }

    @Test
    fun `6 years ferret is Geriatric`() {
        val stage = calculator.determine(ageInYears = 6.0)
        assertEquals(LifeStage.Ferret.Geriatric, stage)
    }

    @ParameterizedTest(name = "{0} y -> {1}")
    @CsvSource(
        "0.01, Kit",
        "0.32, Kit",
        "0.33, Juvenile",
        "0.99, Juvenile",
        "1.0, Adult",
        "2.99, Adult",
        "3.0, Senior",
        "4.99, Senior",
        "5.0, Geriatric",
        "8.0, Geriatric",
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
        val viaParams = calculator.determine(4.0, SpeciesParams.Ferret)
        val viaOverload = calculator.determine(4.0)
        assertEquals(viaOverload, viaParams)
        assertEquals(LifeStage.Ferret.Senior, viaParams)
    }

    @Test
    fun `rejects non-Ferret params`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(4.0, SpeciesParams.Rat)
        }
    }

    @Test
    fun `stage ordinal is non-decreasing in age`() {
        var previousOrdinal = -1
        var age = 0.01
        while (age <= 11.0) {
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
    fun `expectedLifespanRange returns 5 to 10 years`() {
        val range = calculator.expectedLifespanRange()
        assertEquals(5.0, range.start, 0.001)
        assertEquals(10.0, range.endInclusive, 0.001)
    }
}
