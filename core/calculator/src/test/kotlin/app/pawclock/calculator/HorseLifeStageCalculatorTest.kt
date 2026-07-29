package app.pawclock.calculator

import app.pawclock.model.HorseType
import app.pawclock.model.LifeStage
import kotlin.test.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Тесты [HorseLifeStageCalculator] — стадии жизни лошади по возрасту (AAEP, §4.10).
 *
 * Пороги (единые — формула AAEP не зависит от породы; «senior с ~15 лет» по AAEP Senior Horse Care):
 *  - Foal: 0 < age < 1.0
 *  - Yearling: 1.0 ≤ age < 2.0
 *  - YoungAdult: 2.0 ≤ age < 4.0
 *  - Adult: 4.0 ≤ age < 15.0
 *  - Senior: age ≥ 15.0
 */
class HorseLifeStageCalculatorTest {
    private val calculator = HorseLifeStageCalculator

    @Test
    fun `6 months horse is Foal`() {
        assertEquals(LifeStage.Horse.Foal, calculator.determine(0.5))
    }

    @Test
    fun `1 year horse is Yearling`() {
        assertEquals(LifeStage.Horse.Yearling, calculator.determine(1.0))
    }

    @Test
    fun `3 years horse is YoungAdult`() {
        assertEquals(LifeStage.Horse.YoungAdult, calculator.determine(3.0))
    }

    @Test
    fun `8 years horse is Adult`() {
        assertEquals(LifeStage.Horse.Adult, calculator.determine(8.0))
    }

    @Test
    fun `20 years horse is Senior`() {
        assertEquals(LifeStage.Horse.Senior, calculator.determine(20.0))
    }

    @ParameterizedTest(name = "{0} y -> {1}")
    @CsvSource(
        "0.01, Foal",
        "0.99, Foal",
        "1.0, Yearling",
        "1.99, Yearling",
        "2.0, YoungAdult",
        "3.99, YoungAdult",
        "4.0, Adult",
        "14.99, Adult",
        "15.0, Senior",
        "30.0, Senior",
    )
    fun `determine returns expected stage at boundaries`(
        ageInYears: Double,
        expectedStageName: String,
    ) {
        assertEquals(expectedStageName, calculator.determine(ageInYears)::class.simpleName)
    }

    @Test
    fun `determine throws on zero age`() {
        assertThrows<IllegalArgumentException> { calculator.determine(0.0) }
    }

    @Test
    fun `determine throws on negative age`() {
        assertThrows<IllegalArgumentException> { calculator.determine(-1.0) }
    }

    @Test
    fun `unified determine via params matches direct overload`() {
        val viaParams = calculator.determine(20.0, SpeciesParams.Horse(HorseType.Pony))
        val viaOverload = calculator.determine(20.0)
        assertEquals(viaOverload, viaParams)
        assertEquals(LifeStage.Horse.Senior, viaParams)
    }

    @Test
    fun `rejects non-Horse params`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(20.0, SpeciesParams.Rat)
        }
    }

    @Test
    fun `stage ordinal is non-decreasing in age`() {
        var previousOrdinal = -1
        var age = 0.01
        while (age <= 35.0) {
            val stage = calculator.determine(age)
            assertTrue(
                stage.ordinal >= previousOrdinal,
                "age=$age: stage ordinal decreased from $previousOrdinal to ${stage.ordinal}",
            )
            previousOrdinal = stage.ordinal
            age += 0.01
        }
    }

    @Test
    fun `expectedLifespanRange varies by horse type`() {
        val pony = calculator.expectedLifespanRange(HorseType.Pony)
        assertEquals(25.0, pony.start, 0.001)
        assertEquals(35.0, pony.endInclusive, 0.001)

        val draft = calculator.expectedLifespanRange(HorseType.DraftHorse)
        assertEquals(20.0, draft.start, 0.001)
        assertEquals(25.0, draft.endInclusive, 0.001)

        val light = calculator.expectedLifespanRange(HorseType.LightHorse)
        assertEquals(25.0, light.start, 0.001)
        assertEquals(30.0, light.endInclusive, 0.001)
    }
}
