package app.pawclock.calculator

import app.pawclock.model.LifeStage
import app.pawclock.model.RabbitSize
import kotlin.test.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Тесты [RabbitLifeStageCalculator] — стадии жизни кролика по возрасту
 * (Oxbow Rabbit Life Stages, §4.3 спецификации).
 *
 * Пороги (единые для всех пород):
 *  - Infancy: 0 < age < 0.25 (0–3 мес.)
 *  - Adolescence: 0.25 ≤ age < 0.5 (3–6 мес.)
 *  - YoungAdult: 0.5 ≤ age < 1.0 (6–12 мес.)
 *  - Adult: 1.0 ≤ age < 5.0 (1–5 лет)
 *  - Senior: age ≥ 5.0 (5+ лет)
 */
class RabbitLifeStageCalculatorTest {
    private val calculator = RabbitLifeStageCalculator

    @Test
    fun `2 months rabbit is Infancy`() {
        val stage = calculator.determine(ageInYears = 0.166, size = RabbitSize.Medium)
        assertEquals(LifeStage.Rabbit.Infancy, stage)
    }

    @Test
    fun `4 months rabbit is Adolescence`() {
        val stage = calculator.determine(ageInYears = 0.33, size = RabbitSize.Medium)
        assertEquals(LifeStage.Rabbit.Adolescence, stage)
    }

    @Test
    fun `8 months rabbit is YoungAdult`() {
        val stage = calculator.determine(ageInYears = 0.66, size = RabbitSize.Medium)
        assertEquals(LifeStage.Rabbit.YoungAdult, stage)
    }

    @Test
    fun `2 year rabbit is Adult`() {
        val stage = calculator.determine(ageInYears = 2.0, size = RabbitSize.Medium)
        assertEquals(LifeStage.Rabbit.Adult, stage)
    }

    @Test
    fun `6 year rabbit is Senior`() {
        val stage = calculator.determine(ageInYears = 6.0, size = RabbitSize.Medium)
        assertEquals(LifeStage.Rabbit.Senior, stage)
    }

    @ParameterizedTest(name = "{0} y -> {1}")
    @CsvSource(
        "0.01, Infancy",
        "0.24, Infancy",
        "0.25, Adolescence",
        "0.49, Adolescence",
        "0.5, YoungAdult",
        "0.99, YoungAdult",
        "1.0, Adult",
        "4.99, Adult",
        "5.0, Senior",
        "10.0, Senior",
    )
    fun `determine returns expected stage at boundaries`(
        ageInYears: Double,
        expectedStageName: String,
    ) {
        val stage = calculator.determine(ageInYears = ageInYears, size = RabbitSize.Medium)
        assertEquals(expectedStageName, stage::class.simpleName)
    }

    @Test
    fun `determine throws on zero age`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(ageInYears = 0.0, size = RabbitSize.Medium)
        }
    }

    @Test
    fun `determine throws on negative age`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(ageInYears = -1.0, size = RabbitSize.Medium)
        }
    }

    @Test
    fun `unified determine via params matches size overload`() {
        val params = SpeciesParams.Rabbit(size = RabbitSize.Giant)
        val viaParams = calculator.determine(6.0, params)
        val viaOverload = calculator.determine(6.0, RabbitSize.Giant)
        assertEquals(viaOverload, viaParams)
        assertEquals(LifeStage.Rabbit.Senior, viaParams)
    }

    @Test
    fun `rejects non-Rabbit params`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(6.0, SpeciesParams.Cat(app.pawclock.model.CatType.IndoorShortHair))
        }
    }

    @Test
    fun `stage ordinal is non-decreasing in age`() {
        var previousOrdinal = -1
        var age = 0.01
        while (age <= 15.0) {
            val stage = calculator.determine(ageInYears = age, size = RabbitSize.Medium)
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
    fun `expectedLifespanRange Medium returns 8 to 12 years`() {
        val range = calculator.expectedLifespanRange(RabbitSize.Medium)
        assertEquals(8.0, range.start, 0.001)
        assertEquals(12.0, range.endInclusive, 0.001)
    }

    @Test
    fun `expectedLifespanRange Dwarf lives longer than Giant`() {
        val dwarf = calculator.expectedLifespanRange(RabbitSize.Dwarf)
        val giant = calculator.expectedLifespanRange(RabbitSize.Giant)
        assertTrue(
            dwarf.endInclusive > giant.endInclusive,
            "dwarf rabbits should outlive giant rabbits",
        )
    }

    @Test
    fun `expectedLifespanRange Giant returns 6 to 8 years`() {
        val range = calculator.expectedLifespanRange(RabbitSize.Giant)
        assertEquals(6.0, range.start, 0.001)
        assertEquals(8.0, range.endInclusive, 0.001)
    }

    @Test
    fun `expectedLifespanRange small breeds reach up to 14 years`() {
        val dwarf = calculator.expectedLifespanRange(RabbitSize.Dwarf)
        assertEquals(14.0, dwarf.endInclusive, 0.001)
    }
}
