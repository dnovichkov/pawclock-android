package app.pawclock.calculator

import app.pawclock.model.BirdType
import app.pawclock.model.LifeStage
import kotlin.test.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Тесты [BirdLifeStageCalculator] — стадии жизни птицы по доле прожитой жизни (AAV + Lafeber, §4.8).
 *
 * Пороги (доля от видовой ЧЖ):
 *  - Hatchling: < 5 %
 *  - Juvenile: 5–20 %
 *  - Adult: 20–70 %
 *  - Senior: 70–90 %
 *  - Geriatric: 90 %+
 *
 * Граничные кейсы рассчитаны для Budgerigar (ЧЖ 7 лет): доли 0.05/0.20/0.70/0.90
 * соответствуют возрастам 0.35 / 1.4 / 4.9 / 6.3 года.
 */
class BirdLifeStageCalculatorTest {
    private val calculator = BirdLifeStageCalculator

    @Test
    fun `young budgerigar is Hatchling`() {
        // 0.1/7 ≈ 1.4 % < 5 %
        assertEquals(LifeStage.Bird.Hatchling, calculator.determine(0.1, BirdType.Budgerigar))
    }

    @Test
    fun `half-year budgerigar is Juvenile`() {
        // 0.5/7 ≈ 7.1 %
        assertEquals(LifeStage.Bird.Juvenile, calculator.determine(0.5, BirdType.Budgerigar))
    }

    @Test
    fun `two-year budgerigar is Adult`() {
        // 2/7 ≈ 28.6 %
        assertEquals(LifeStage.Bird.Adult, calculator.determine(2.0, BirdType.Budgerigar))
    }

    @Test
    fun `five-year budgerigar is Senior`() {
        // 5/7 ≈ 71.4 %
        assertEquals(LifeStage.Bird.Senior, calculator.determine(5.0, BirdType.Budgerigar))
    }

    @Test
    fun `old budgerigar is Geriatric`() {
        // 6.5/7 ≈ 92.9 %
        assertEquals(LifeStage.Bird.Geriatric, calculator.determine(6.5, BirdType.Budgerigar))
    }

    @Test
    fun `stage scales with species lifespan`() {
        // Для долгоживущего ара (ЧЖ 50) 15 лет = 30 % → Adult, а не Senior.
        assertEquals(LifeStage.Bird.Adult, calculator.determine(15.0, BirdType.Macaw))
        // 40/50 = 80 % → Senior
        assertEquals(LifeStage.Bird.Senior, calculator.determine(40.0, BirdType.Macaw))
    }

    @ParameterizedTest(name = "budgerigar {0} y -> {1}")
    // Значения подобраны заведомо внутри полос (не на математической границе доли),
    // чтобы избежать неоднозначности IEEE-754 при делении ровно на порог.
    @CsvSource(
        "0.1, Hatchling",
        "0.34, Hatchling",
        "0.36, Juvenile",
        "1.39, Juvenile",
        "1.45, Adult",
        "4.89, Adult",
        "4.95, Senior",
        "6.29, Senior",
        "6.35, Geriatric",
        "9.0, Geriatric",
    )
    fun `determine returns expected stage at fraction boundaries`(
        ageInYears: Double,
        expectedStageName: String,
    ) {
        val stage = calculator.determine(ageInYears, BirdType.Budgerigar)
        assertEquals(expectedStageName, stage::class.simpleName)
    }

    @Test
    fun `determine throws on zero age`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(0.0, BirdType.Budgerigar)
        }
    }

    @Test
    fun `determine throws on negative age`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(-1.0, BirdType.Budgerigar)
        }
    }

    @Test
    fun `unified determine via params matches direct overload`() {
        val viaParams = calculator.determine(5.0, SpeciesParams.Bird(BirdType.Budgerigar))
        val viaOverload = calculator.determine(5.0, BirdType.Budgerigar)
        assertEquals(viaOverload, viaParams)
        assertEquals(LifeStage.Bird.Senior, viaParams)
    }

    @Test
    fun `rejects non-Bird params`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(5.0, SpeciesParams.Rat)
        }
    }

    @Test
    fun `stage ordinal is non-decreasing in age`() {
        var previousOrdinal = -1
        var age = 0.01
        while (age <= 7.0) {
            val stage = calculator.determine(age, BirdType.Budgerigar)
            assertTrue(
                stage.ordinal >= previousOrdinal,
                "age=$age: stage ordinal decreased from $previousOrdinal to ${stage.ordinal}",
            )
            previousOrdinal = stage.ordinal
            age += 0.01
        }
    }

    @Test
    fun `expectedLifespanRange for budgerigar is 5 to 10 years`() {
        val range = calculator.expectedLifespanRange(BirdType.Budgerigar)
        assertEquals(5.0, range.start, 0.001)
        assertEquals(10.0, range.endInclusive, 0.001)
    }

    @Test
    fun `expectedLifespanRange for amazon reaches decades`() {
        val range = calculator.expectedLifespanRange(BirdType.Amazon)
        assertTrue(range.endInclusive >= 40.0, "Amazon lifespan upper bound should reach decades")
    }
}
