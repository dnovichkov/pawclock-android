package app.pawclock.calculator

import app.pawclock.model.CatType
import app.pawclock.model.LifeStage
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Тесты [CatLifeStageCalculator] — определение стадии жизни кошки по возрасту и типу
 * содержания согласно AAHA/AAFP 2021 Feline Life Stage Guidelines (§4.2 спецификации).
 *
 * Стадии (от младшей к старшей):
 *  - Kitten: 0 < age ≤ 1 (граница 1.0 включительно в Kitten)
 *  - YoungAdult: 1 < age < 7
 *  - MatureAdult: 7 ≤ age < 11 (включая граничное 10.x)
 *  - Senior: 11 ≤ age < endOfLifeStart (значение AAFP 2021 «Senior 10+» округлено к 11-му году
 *    жизни — см. KDoc [CatLifeStageCalculator])
 *  - EndOfLife: age ≥ endOfLifeStart (зависит от [CatType]:
 *    Indoor ≈ 16, Outdoor ≈ 4, LargeBreed ≈ 13)
 */
class CatLifeStageCalculatorTest {
    private val calculator = CatLifeStageCalculator()

    // ===== Базовые кейсы из плана (Task 10) =====

    @Test
    fun `0_5 year cat is Kitten`() {
        val stage = calculator.determine(ageInYears = 0.5, catType = CatType.IndoorShortHair)
        assertEquals(LifeStage.Cat.Kitten, stage)
    }

    @Test
    fun `1 year cat is Kitten at boundary`() {
        // Спецификация: Kitten = 0–1, граничное значение 1.0 включительно в Kitten.
        val stage = calculator.determine(ageInYears = 1.0, catType = CatType.IndoorShortHair)
        assertEquals(LifeStage.Cat.Kitten, stage)
    }

    @Test
    fun `2 year cat is YoungAdult`() {
        val stage = calculator.determine(ageInYears = 2.0, catType = CatType.IndoorShortHair)
        assertEquals(LifeStage.Cat.YoungAdult, stage)
    }

    @Test
    fun `7 year cat is MatureAdult`() {
        val stage = calculator.determine(ageInYears = 7.0, catType = CatType.IndoorShortHair)
        assertEquals(LifeStage.Cat.MatureAdult, stage)
    }

    @Test
    fun `10 year cat is MatureAdult at boundary`() {
        // Спецификация: «Senior 10+» интерпретируется как 11-й год жизни (см. KDoc).
        val stage = calculator.determine(ageInYears = 10.0, catType = CatType.IndoorShortHair)
        assertEquals(LifeStage.Cat.MatureAdult, stage)
    }

    @Test
    fun `12 year cat is Senior`() {
        val stage = calculator.determine(ageInYears = 12.0, catType = CatType.IndoorShortHair)
        assertEquals(LifeStage.Cat.Senior, stage)
    }

    // ===== Параметризованные тесты для всех границ × CatType =====

    @ParameterizedTest(name = "{0} y, {1} -> {2}")
    @CsvSource(
        // --- IndoorShortHair (lifespan 12–18, EndOfLife с 16) ---
        "0.1,IndoorShortHair,Kitten",
        "0.5,IndoorShortHair,Kitten",
        "1.0,IndoorShortHair,Kitten",
        "1.01,IndoorShortHair,YoungAdult",
        "2.0,IndoorShortHair,YoungAdult",
        "6.99,IndoorShortHair,YoungAdult",
        "7.0,IndoorShortHair,MatureAdult",
        "10.0,IndoorShortHair,MatureAdult",
        "10.99,IndoorShortHair,MatureAdult",
        "11.0,IndoorShortHair,Senior",
        "15.99,IndoorShortHair,Senior",
        "16.0,IndoorShortHair,EndOfLife",
        "20.0,IndoorShortHair,EndOfLife",
        // --- IndoorLongHair (same lifespan as ShortHair) ---
        "0.5,IndoorLongHair,Kitten",
        "1.0,IndoorLongHair,Kitten",
        "5.0,IndoorLongHair,YoungAdult",
        "8.0,IndoorLongHair,MatureAdult",
        "12.0,IndoorLongHair,Senior",
        "16.0,IndoorLongHair,EndOfLife",
        // --- Outdoor (lifespan 2–5, EndOfLife с 4) ---
        // Стандартные возрастные пороги стадий те же (AAFP по абсолютному возрасту),
        // но EndOfLife наступает гораздо раньше — почти все уличные кошки умирают
        // до достижения Senior-стадии.
        "0.5,Outdoor,Kitten",
        "1.0,Outdoor,Kitten",
        "2.0,Outdoor,YoungAdult",
        "3.99,Outdoor,YoungAdult",
        "4.0,Outdoor,EndOfLife",
        "5.0,Outdoor,EndOfLife",
        // --- LargeBreed (lifespan ≈ 12–15, EndOfLife с 13) ---
        "0.5,LargeBreed,Kitten",
        "1.0,LargeBreed,Kitten",
        "2.0,LargeBreed,YoungAdult",
        "7.0,LargeBreed,MatureAdult",
        "11.0,LargeBreed,Senior",
        "12.99,LargeBreed,Senior",
        "13.0,LargeBreed,EndOfLife",
        "15.0,LargeBreed,EndOfLife",
    )
    fun `determine returns expected stage for given age and cat type`(
        ageInYears: Double,
        catTypeName: String,
        expectedStageName: String,
    ) {
        val catType = CatType.valueOf(catTypeName)
        val stage = calculator.determine(ageInYears = ageInYears, catType = catType)
        assertEquals(expectedStageName, stage::class.simpleName)
    }

    // ===== Edge cases =====

    @Test
    fun `determine throws on zero age`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(ageInYears = 0.0, catType = CatType.IndoorShortHair)
        }
    }

    @Test
    fun `determine throws on negative age`() {
        assertThrows<IllegalArgumentException> {
            calculator.determine(ageInYears = -1.0, catType = CatType.IndoorShortHair)
        }
    }

    @Test
    fun `very small age maps to Kitten`() {
        // 1 неделя ≈ 0.019 года — всё ещё Kitten.
        val stage = calculator.determine(ageInYears = 0.02, catType = CatType.IndoorShortHair)
        assertEquals(LifeStage.Cat.Kitten, stage)
    }

    @Test
    fun `very large age maps to EndOfLife regardless of cat type`() {
        // 50 лет — за пределами любого expected lifespan: должно мапиться в EndOfLife.
        for (catType in CatType.entries) {
            val stage = calculator.determine(ageInYears = 50.0, catType = catType)
            assertEquals(LifeStage.Cat.EndOfLife, stage, "catType=$catType: expected EndOfLife")
        }
    }

    @Test
    fun `outdoor cat reaches EndOfLife earlier than indoor cat at same age`() {
        // Биологический инвариант: уличные кошки достигают EndOfLife раньше.
        val outdoorAt5 = calculator.determine(ageInYears = 5.0, catType = CatType.Outdoor)
        val indoorAt5 = calculator.determine(ageInYears = 5.0, catType = CatType.IndoorShortHair)
        assertEquals(LifeStage.Cat.EndOfLife, outdoorAt5)
        assertEquals(LifeStage.Cat.YoungAdult, indoorAt5)
        assertTrue(
            outdoorAt5.ordinal > indoorAt5.ordinal,
            "outdoor cat at age 5 should be in later stage than indoor",
        )
    }

    @Test
    fun `large breed cat reaches EndOfLife earlier than indoor short hair at same age`() {
        // LargeBreed lifespan ≈ 12-15, на 2-3 года короче IndoorShortHair (12-18).
        val largeBreedAt15 = calculator.determine(ageInYears = 15.0, catType = CatType.LargeBreed)
        val indoorAt15 = calculator.determine(ageInYears = 15.0, catType = CatType.IndoorShortHair)
        assertEquals(LifeStage.Cat.EndOfLife, largeBreedAt15)
        assertEquals(LifeStage.Cat.Senior, indoorAt15)
    }

    // ===== Тесты expectedLifespanRange =====

    @Test
    fun `expectedLifespanRange IndoorShortHair returns 12 to 18 years`() {
        val range = calculator.expectedLifespanRange(CatType.IndoorShortHair)
        assertDoubleEquals(12.0, range.start, 0.001)
        assertDoubleEquals(18.0, range.endInclusive, 0.001)
    }

    @Test
    fun `expectedLifespanRange IndoorLongHair returns 12 to 18 years`() {
        val range = calculator.expectedLifespanRange(CatType.IndoorLongHair)
        assertDoubleEquals(12.0, range.start, 0.001)
        assertDoubleEquals(18.0, range.endInclusive, 0.001)
    }

    @Test
    fun `expectedLifespanRange Outdoor returns 2 to 5 years`() {
        val range = calculator.expectedLifespanRange(CatType.Outdoor)
        assertDoubleEquals(2.0, range.start, 0.001)
        assertDoubleEquals(5.0, range.endInclusive, 0.001)
    }

    @Test
    fun `expectedLifespanRange LargeBreed returns 12 to 15 years`() {
        // Maine Coon median ≈ 12.5 лет, верхняя граница ≈ 15. Слегка короче indoor
        // из-за повышенной нагрузки на сердечно-сосудистую систему у крупных пород.
        val range = calculator.expectedLifespanRange(CatType.LargeBreed)
        assertDoubleEquals(12.0, range.start, 0.001)
        assertDoubleEquals(15.0, range.endInclusive, 0.001)
    }

    @Test
    fun `expectedLifespanRange indoor cats live longer than outdoor cats`() {
        // Биологический инвариант: домашние кошки живут существенно дольше уличных.
        val indoor = calculator.expectedLifespanRange(CatType.IndoorShortHair)
        val outdoor = calculator.expectedLifespanRange(CatType.Outdoor)
        assertTrue(indoor.start > outdoor.endInclusive)
        assertTrue(indoor.endInclusive > outdoor.endInclusive)
    }

    // ===== Монотонность по возрасту (smoke property check до Task 11) =====

    @Test
    fun `stage ordinal is non-decreasing in age for each cat type`() {
        // Для всех типов кошек: при росте возраста ordinal стадии не уменьшается.
        for (catType in CatType.entries) {
            var previousOrdinal = -1
            var age = 0.1
            while (age <= 25.0) {
                val stage = calculator.determine(ageInYears = age, catType = catType)
                assertTrue(
                    stage.ordinal >= previousOrdinal,
                    "catType=$catType, age=$age: stage ordinal decreased from $previousOrdinal to ${stage.ordinal}",
                )
                previousOrdinal = stage.ordinal
                age += 0.1
            }
        }
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
