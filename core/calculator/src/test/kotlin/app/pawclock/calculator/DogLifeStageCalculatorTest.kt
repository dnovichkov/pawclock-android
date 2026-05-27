package app.pawclock.calculator

import app.pawclock.model.DogSize
import app.pawclock.model.LifeStage
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Тесты [DogLifeStageCalculator] — определение стадии жизни собаки по возрасту и размеру
 * согласно AAHA 2019 Canine Life Stage Guidelines (§4.1 спецификации).
 *
 * Стадии (от младшей к старшей):
 *  - Puppy: 0 до ~0.75 года (половая зрелость)
 *  - YoungAdult: ~0.75 до 3 лет (социальная зрелость)
 *  - MatureAdult: 3 года до порога senior (зависит от размера)
 *  - Senior: Toy/Small 11+, Medium 9+, Large 7+, Giant 5+
 *  - EndOfLife: близко к expected lifespan (верхняя граница из McMillan 2024)
 */
class DogLifeStageCalculatorTest {
    private val calculator = DogLifeStageCalculator()

    // ===== Базовые кейсы из плана =====

    @Test
    fun `Toy puppy at 6 months is Puppy`() {
        val stage = calculator.determine(ageInYears = 0.5, size = DogSize.Toy)
        assertEquals(LifeStage.Dog.Puppy, stage)
    }

    @Test
    fun `Small adult at 4 years is MatureAdult`() {
        val stage = calculator.determine(ageInYears = 4.0, size = DogSize.Small)
        assertEquals(LifeStage.Dog.MatureAdult, stage)
    }

    @Test
    fun `Large senior at 7 years is Senior`() {
        val stage = calculator.determine(ageInYears = 7.0, size = DogSize.Large)
        assertEquals(LifeStage.Dog.Senior, stage)
    }

    @Test
    fun `Giant senior at 5 years is Senior`() {
        val stage = calculator.determine(ageInYears = 5.0, size = DogSize.Giant)
        assertEquals(LifeStage.Dog.Senior, stage)
    }

    @Test
    fun `Toy at 15 years is EndOfLife`() {
        // Toy expected lifespan upper bound = 18 лет, EndOfLife threshold = 15 лет (последние ~3 года жизни).
        val stage = calculator.determine(ageInYears = 15.0, size = DogSize.Toy)
        assertEquals(LifeStage.Dog.EndOfLife, stage)
    }

    // ===== Параметризованные тесты для всех границ size × stage =====

    @ParameterizedTest(name = "{0} y, {1} -> {2}")
    @CsvSource(
        // --- Toy (lifespan 12–18 лет, senior с 11) ---
        "0.1,Toy,Puppy",
        "0.5,Toy,Puppy",
        "0.74,Toy,Puppy",
        "0.75,Toy,YoungAdult",
        "1.0,Toy,YoungAdult",
        "2.99,Toy,YoungAdult",
        "3.0,Toy,MatureAdult",
        "10.99,Toy,MatureAdult",
        "11.0,Toy,Senior",
        "14.99,Toy,Senior",
        "15.0,Toy,EndOfLife",
        // --- Small (lifespan 12–18, senior с 11) ---
        "0.5,Small,Puppy",
        "1.0,Small,YoungAdult",
        "3.0,Small,MatureAdult",
        "11.0,Small,Senior",
        "15.0,Small,EndOfLife",
        // --- Medium (lifespan 10–13, senior с 9) ---
        "0.5,Medium,Puppy",
        "1.0,Medium,YoungAdult",
        "3.0,Medium,MatureAdult",
        "8.99,Medium,MatureAdult",
        "9.0,Medium,Senior",
        "10.99,Medium,Senior",
        "11.0,Medium,EndOfLife",
        // --- Large (lifespan 8–12, senior с 7) ---
        "0.5,Large,Puppy",
        "1.0,Large,YoungAdult",
        "3.0,Large,MatureAdult",
        "6.99,Large,MatureAdult",
        "7.0,Large,Senior",
        "9.99,Large,Senior",
        "10.0,Large,EndOfLife",
        // --- Giant (lifespan 6–8, senior с 5) ---
        "0.5,Giant,Puppy",
        "1.0,Giant,YoungAdult",
        "3.0,Giant,MatureAdult",
        "4.99,Giant,MatureAdult",
        "5.0,Giant,Senior",
        "6.99,Giant,Senior",
        "7.0,Giant,EndOfLife",
    )
    fun `determine returns expected stage for given age and size`(
        ageInYears: Double,
        sizeName: String,
        expectedStageName: String,
    ) {
        val size = DogSize.valueOf(sizeName)
        val stage = calculator.determine(ageInYears = ageInYears, size = size)
        assertEquals(expectedStageName, stage::class.simpleName)
    }

    // ===== Edge cases =====

    @Test
    fun `determine throws on zero age`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(ageInYears = 0.0, size = DogSize.Medium)
        }
    }

    @Test
    fun `determine throws on negative age`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(ageInYears = -1.0, size = DogSize.Medium)
        }
    }

    @Test
    fun `very small age maps to Puppy`() {
        // 1 неделя ≈ 0.019 года — всё ещё Puppy.
        val stage = calculator.determine(ageInYears = 0.02, size = DogSize.Medium)
        assertEquals(LifeStage.Dog.Puppy, stage)
    }

    @Test
    fun `very large age maps to EndOfLife regardless of size`() {
        // 50 лет — за пределами любого expected lifespan: должно мапиться в EndOfLife.
        for (size in DogSize.entries) {
            val stage = calculator.determine(ageInYears = 50.0, size = size)
            assertEquals(LifeStage.Dog.EndOfLife, stage, "size=$size: expected EndOfLife")
        }
    }

    // ===== Тесты expectedLifespanRange =====

    @Test
    fun `expectedLifespanRange Toy returns 12 to 18 years`() {
        val range = calculator.expectedLifespanRange(DogSize.Toy)
        assertDoubleEquals(12.0, range.start, 0.001)
        assertDoubleEquals(18.0, range.endInclusive, 0.001)
    }

    @Test
    fun `expectedLifespanRange Small returns 12 to 18 years`() {
        val range = calculator.expectedLifespanRange(DogSize.Small)
        assertDoubleEquals(12.0, range.start, 0.001)
        assertDoubleEquals(18.0, range.endInclusive, 0.001)
    }

    @Test
    fun `expectedLifespanRange Medium returns 10 to 13 years`() {
        val range = calculator.expectedLifespanRange(DogSize.Medium)
        assertDoubleEquals(10.0, range.start, 0.001)
        assertDoubleEquals(13.0, range.endInclusive, 0.001)
    }

    @Test
    fun `expectedLifespanRange Large returns 8 to 12 years`() {
        val range = calculator.expectedLifespanRange(DogSize.Large)
        assertDoubleEquals(8.0, range.start, 0.001)
        assertDoubleEquals(12.0, range.endInclusive, 0.001)
    }

    @Test
    fun `expectedLifespanRange Giant returns 6 to 8 years`() {
        val range = calculator.expectedLifespanRange(DogSize.Giant)
        assertDoubleEquals(6.0, range.start, 0.001)
        assertDoubleEquals(8.0, range.endInclusive, 0.001)
    }

    @Test
    fun `expectedLifespanRange smaller dogs live longer than larger ones`() {
        // Биологический инвариант: меньшие собаки живут дольше.
        val toy = calculator.expectedLifespanRange(DogSize.Toy)
        val medium = calculator.expectedLifespanRange(DogSize.Medium)
        val large = calculator.expectedLifespanRange(DogSize.Large)
        val giant = calculator.expectedLifespanRange(DogSize.Giant)
        assertTrue(toy.endInclusive >= medium.endInclusive)
        assertTrue(medium.endInclusive >= large.endInclusive)
        assertTrue(large.endInclusive >= giant.endInclusive)
    }

    private fun assertDoubleEquals(
        expected: Double,
        actual: Double,
        tolerance: Double,
    ) {
        assertTrue(
            kotlin.math.abs(expected - actual) <= tolerance,
            "expected $expected ± $tolerance, but was $actual",
        )
    }
}
