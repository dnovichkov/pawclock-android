package app.pawclock.calculator

import app.pawclock.model.FishType
import app.pawclock.model.LifeStage
import kotlin.test.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Тесты [FishLifeStageCalculator] — стадии жизни рыбы по доле прожитой жизни
 * (PetMD + AquariumStoreDepot, §4.11). 4 фазы.
 *
 * Пороги (доля от видовой ЧЖ):
 *  - Fry: < 5 %
 *  - Juvenile: 5–20 %
 *  - Adult: 20–75 %
 *  - Senior: 75 %+
 *
 * Граничные кейсы рассчитаны для Koi (ЧЖ 30 лет): доли 0.05/0.20/0.75
 * соответствуют возрастам 1.5 / 6 / 22.5 лет.
 */
class FishLifeStageCalculatorTest {
    private val calculator = FishLifeStageCalculator

    @Test
    fun `young koi is Fry`() {
        // 1/30 ≈ 3.3 % < 5 %
        assertEquals(LifeStage.Fish.Fry, calculator.determine(1.0, FishType.Koi))
    }

    @Test
    fun `three-year koi is Juvenile`() {
        // 3/30 = 10 %
        assertEquals(LifeStage.Fish.Juvenile, calculator.determine(3.0, FishType.Koi))
    }

    @Test
    fun `ten-year koi is Adult`() {
        // 10/30 ≈ 33 %
        assertEquals(LifeStage.Fish.Adult, calculator.determine(10.0, FishType.Koi))
    }

    @Test
    fun `old koi is Senior`() {
        // 27/30 = 90 %
        assertEquals(LifeStage.Fish.Senior, calculator.determine(27.0, FishType.Koi))
    }

    @Test
    fun `stage scales with species lifespan`() {
        // Для короткоживущей гуппи (ЧЖ 2) 1.8 года = 90 % → Senior.
        assertEquals(LifeStage.Fish.Senior, calculator.determine(1.8, FishType.Guppy))
        // 0.8/2 = 40 % → Adult
        assertEquals(LifeStage.Fish.Adult, calculator.determine(0.8, FishType.Guppy))
    }

    @ParameterizedTest(name = "koi {0} y -> {1}")
    // Значения подобраны заведомо внутри полос (не на математической границе доли).
    @CsvSource(
        "1.0, Fry",
        "1.4, Fry",
        "1.6, Juvenile",
        "5.9, Juvenile",
        "6.1, Adult",
        "22.0, Adult",
        "23.0, Senior",
        "30.0, Senior",
    )
    fun `determine returns expected stage at fraction boundaries`(
        ageInYears: Double,
        expectedStageName: String,
    ) {
        val stage = calculator.determine(ageInYears, FishType.Koi)
        assertEquals(expectedStageName, stage::class.simpleName)
    }

    @Test
    fun `determine throws on zero age`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(0.0, FishType.Koi)
        }
    }

    @Test
    fun `determine throws on negative age`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(-1.0, FishType.Koi)
        }
    }

    @Test
    fun `unified determine via params matches direct overload`() {
        val viaParams = calculator.determine(27.0, SpeciesParams.Fish(FishType.Koi))
        val viaOverload = calculator.determine(27.0, FishType.Koi)
        assertEquals(viaOverload, viaParams)
        assertEquals(LifeStage.Fish.Senior, viaParams)
    }

    @Test
    fun `rejects non-Fish params`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(5.0, SpeciesParams.Rat)
        }
    }

    @Test
    fun `stage ordinal is non-decreasing in age`() {
        var previousOrdinal = -1
        var age = 0.01
        while (age <= 30.0) {
            val stage = calculator.determine(age, FishType.Koi)
            assertTrue(
                stage.ordinal >= previousOrdinal,
                "age=$age: stage ordinal decreased from $previousOrdinal to ${stage.ordinal}",
            )
            previousOrdinal = stage.ordinal
            age += 0.01
        }
    }

    @Test
    fun `expectedLifespanRange for guppy is short`() {
        val range = calculator.expectedLifespanRange(FishType.Guppy)
        assertEquals(1.0, range.start, 0.001)
        assertEquals(3.0, range.endInclusive, 0.001)
    }

    @Test
    fun `expectedLifespanRange for koi reaches decades`() {
        val range = calculator.expectedLifespanRange(FishType.Koi)
        assertTrue(range.endInclusive >= 35.0, "Koi lifespan upper bound should reach 35+ years")
    }
}
