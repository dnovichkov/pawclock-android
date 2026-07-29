package app.pawclock.calculator

import app.pawclock.model.FishType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * TDD-тесты скалярной формулы рыбы (PetMD + Kodama Koi Farm + AquariumStoreDepot, §4.11 спецификации).
 *
 * Формула `ЧГ = age · 80 / lifespan` (общий [ScalarRatioFormula]), без поправки молодняка
 * (как у рептилии §4.9).
 *
 * Эталонные значения (§4.11):
 *  - Guppy (ЧЖ 2): 1 г. = 40 ЧГ
 *  - Betta (ЧЖ 5): 3 г. = 48
 *  - Goldfish (ЧЖ 15): 5 л. ≈ 26.7
 *  - Koi (ЧЖ 30): 10 л. ≈ 26.7
 *  - Tropical neon / NeonTetra (ЧЖ 8): 2 г. = 20
 */
class FishAgeCalculatorTest {
    private val calculator = FishAgeCalculator

    @Test
    fun `guppy 1 year equals 40 human years`() {
        // 1·80/2 = 40
        val result = calculator.toHumanYears(ageInYears = 1.0, type = FishType.Guppy)
        assertEquals(40.0, result, 0.01)
    }

    @Test
    fun `betta 3 years equals 48 human years`() {
        // 3·80/5 = 48
        val result = calculator.toHumanYears(ageInYears = 3.0, type = FishType.Betta)
        assertEquals(48.0, result, 0.01)
    }

    @Test
    fun `goldfish 5 years equals 26_7 human years`() {
        // 5·80/15 ≈ 26.67
        val result = calculator.toHumanYears(ageInYears = 5.0, type = FishType.Goldfish)
        assertEquals(26.67, result, 0.05)
    }

    @Test
    fun `koi 10 years equals 26_7 human years`() {
        // 10·80/30 ≈ 26.67
        val result = calculator.toHumanYears(ageInYears = 10.0, type = FishType.Koi)
        assertEquals(26.67, result, 0.05)
    }

    @Test
    fun `neon tetra 2 years equals 20 human years`() {
        // 2·80/8 = 20
        val result = calculator.toHumanYears(ageInYears = 2.0, type = FishType.NeonTetra)
        assertEquals(20.0, result, 0.01)
    }

    @Test
    fun `throws on zero age`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(0.0, FishType.Goldfish)
        }
    }

    @Test
    fun `throws on negative age`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(-1.0, FishType.Goldfish)
        }
    }

    @Test
    fun `unified toHumanYears via params matches direct overload`() {
        val viaParams = calculator.toHumanYears(3.0, SpeciesParams.Fish(FishType.Betta))
        val viaOverload = calculator.toHumanYears(3.0, FishType.Betta)
        assertEquals(viaOverload, viaParams, 1e-9)
        assertEquals(48.0, viaParams, 0.01)
    }

    @Test
    fun `rejects non-Fish params`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(3.0, SpeciesParams.Rat)
        }
    }

    @ParameterizedTest(name = "{0} at {1} years = {2} human years")
    @CsvSource(
        "guppy, 1.0, 40.0",
        "betta, 3.0, 48.0",
        "goldfish, 5.0, 26.667",
        "koi, 10.0, 26.667",
        "neon_tetra, 2.0, 20.0",
        "angelfish, 5.0, 40.0",
        "discus, 4.0, 32.0",
    )
    fun `human years for table ages`(
        typeId: String,
        fishAge: Double,
        expectedHuman: Double,
    ) {
        val type = FishType.fromId(typeId)!!
        val result = calculator.toHumanYears(fishAge, type)
        assertEquals(expectedHuman, result, 0.05)
    }

    @Test
    fun `result is monotonically increasing in age for fixed type`() {
        var previous = Double.NEGATIVE_INFINITY
        for (ageHundredths in 1..3000) {
            val age = ageHundredths / 100.0
            val current = calculator.toHumanYears(age, FishType.Koi)
            assert(current > previous) {
                "Non-monotonic at age=$age: previous=$previous, current=$current"
            }
            previous = current
        }
    }

    @Test
    fun `ratio to lifespan invariant holds`() {
        // По определению скалярной формулы compute(age, lifespan)/age == 80/lifespan.
        val goldfish = calculator.toHumanYears(7.0, FishType.Goldfish)
        assertEquals(80.0 / 15.0, goldfish / 7.0, 1e-9)
    }
}
