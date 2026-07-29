package app.pawclock.calculator

import app.pawclock.model.LifeStage
import app.pawclock.model.ReptileType
import kotlin.test.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Тесты [ReptileLifeStageCalculator] — стадии жизни рептилии по доле прожитой жизни
 * (PetPlace + Reptile Centre, §4.9). 4 фазы.
 *
 * Пороги (доля от видовой ЧЖ):
 *  - Hatchling: < 5 %
 *  - Juvenile: 5–20 %
 *  - Adult: 20–75 %
 *  - Senior: 75 %+
 *
 * Граничные кейсы рассчитаны для BoxTurtle (ЧЖ 40 лет): доли 0.05/0.20/0.75
 * соответствуют возрастам 2 / 8 / 30 лет.
 */
class ReptileLifeStageCalculatorTest {
    private val calculator = ReptileLifeStageCalculator

    @Test
    fun `young box turtle is Hatchling`() {
        // 1/40 = 2.5 % < 5 %
        assertEquals(LifeStage.Reptile.Hatchling, calculator.determine(1.0, ReptileType.BoxTurtle))
    }

    @Test
    fun `three-year box turtle is Juvenile`() {
        // 3/40 = 7.5 %
        assertEquals(LifeStage.Reptile.Juvenile, calculator.determine(3.0, ReptileType.BoxTurtle))
    }

    @Test
    fun `fifteen-year box turtle is Adult`() {
        // 15/40 = 37.5 %
        assertEquals(LifeStage.Reptile.Adult, calculator.determine(15.0, ReptileType.BoxTurtle))
    }

    @Test
    fun `old box turtle is Senior`() {
        // 35/40 = 87.5 %
        assertEquals(LifeStage.Reptile.Senior, calculator.determine(35.0, ReptileType.BoxTurtle))
    }

    @Test
    fun `stage scales with species lifespan`() {
        // Для короткоживущего геккона (ЧЖ 15) 12 лет = 80 % → Senior.
        assertEquals(LifeStage.Reptile.Senior, calculator.determine(12.0, ReptileType.LeopardGecko))
        // 5/15 ≈ 33 % → Adult
        assertEquals(LifeStage.Reptile.Adult, calculator.determine(5.0, ReptileType.LeopardGecko))
    }

    @ParameterizedTest(name = "box turtle {0} y -> {1}")
    // Значения подобраны заведомо внутри полос (не на математической границе доли).
    @CsvSource(
        "1.0, Hatchling",
        "1.9, Hatchling",
        "2.1, Juvenile",
        "7.9, Juvenile",
        "8.1, Adult",
        "29.0, Adult",
        "31.0, Senior",
        "40.0, Senior",
    )
    fun `determine returns expected stage at fraction boundaries`(
        ageInYears: Double,
        expectedStageName: String,
    ) {
        val stage = calculator.determine(ageInYears, ReptileType.BoxTurtle)
        assertEquals(expectedStageName, stage::class.simpleName)
    }

    @Test
    fun `determine throws on zero age`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(0.0, ReptileType.BoxTurtle)
        }
    }

    @Test
    fun `determine throws on negative age`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(-1.0, ReptileType.BoxTurtle)
        }
    }

    @Test
    fun `unified determine via params matches direct overload`() {
        val viaParams = calculator.determine(35.0, SpeciesParams.Reptile(ReptileType.BoxTurtle))
        val viaOverload = calculator.determine(35.0, ReptileType.BoxTurtle)
        assertEquals(viaOverload, viaParams)
        assertEquals(LifeStage.Reptile.Senior, viaParams)
    }

    @Test
    fun `rejects non-Reptile params`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(5.0, SpeciesParams.Rat)
        }
    }

    @Test
    fun `stage ordinal is non-decreasing in age`() {
        var previousOrdinal = -1
        var age = 0.01
        while (age <= 40.0) {
            val stage = calculator.determine(age, ReptileType.BoxTurtle)
            assertTrue(
                stage.ordinal >= previousOrdinal,
                "age=$age: stage ordinal decreased from $previousOrdinal to ${stage.ordinal}",
            )
            previousOrdinal = stage.ordinal
            age += 0.01
        }
    }

    @Test
    fun `expectedLifespanRange for leopard gecko is short`() {
        val range = calculator.expectedLifespanRange(ReptileType.LeopardGecko)
        assertEquals(10.0, range.start, 0.001)
        assertEquals(20.0, range.endInclusive, 0.001)
    }

    @Test
    fun `expectedLifespanRange for box turtle reaches decades`() {
        val range = calculator.expectedLifespanRange(ReptileType.BoxTurtle)
        assertTrue(range.endInclusive >= 40.0, "Box turtle lifespan upper bound should reach decades")
    }
}
