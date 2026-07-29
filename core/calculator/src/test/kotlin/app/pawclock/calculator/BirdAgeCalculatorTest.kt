package app.pawclock.calculator

import app.pawclock.model.BirdType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * TDD-тесты скалярной формулы птицы (AAV + Lafeber Vet, §4.8 спецификации).
 *
 * Формула `ЧГ = age · 80 / lifespan`, × 1.3 для возраста < 6 месяцев.
 *
 * Эталонные значения (§4.8):
 *  - Budgerigar (ЧЖ 7): 1 г. ≈ 11.4 ЧГ; 3 г. ≈ 34.3
 *  - Cockatiel (ЧЖ 15): 5 л. ≈ 26.7
 *  - Macaw / Amazon (ЧЖ 50): 10 л. = 16
 *  - Canary (ЧЖ 10): 2 г. = 16
 *  - Budgerigar 6 недель (0.115 г.): ≈ 1.7 ЧГ (поправка × 1.3)
 */
class BirdAgeCalculatorTest {
    private val calculator = BirdAgeCalculator

    @Test
    fun `budgerigar 1 year equals 11_4 human years`() {
        val result = calculator.toHumanYears(ageInYears = 1.0, type = BirdType.Budgerigar)
        assertEquals(11.43, result, 0.05)
    }

    @Test
    fun `budgerigar 3 years equals 34_3 human years`() {
        val result = calculator.toHumanYears(ageInYears = 3.0, type = BirdType.Budgerigar)
        assertEquals(34.29, result, 0.05)
    }

    @Test
    fun `cockatiel 5 years equals 26_7 human years`() {
        val result = calculator.toHumanYears(ageInYears = 5.0, type = BirdType.Cockatiel)
        assertEquals(26.67, result, 0.05)
    }

    @Test
    fun `macaw 10 years equals 16 human years`() {
        val result = calculator.toHumanYears(ageInYears = 10.0, type = BirdType.Macaw)
        assertEquals(16.0, result, 0.01)
    }

    @Test
    fun `canary 2 years equals 16 human years`() {
        val result = calculator.toHumanYears(ageInYears = 2.0, type = BirdType.Canary)
        assertEquals(16.0, result, 0.01)
    }

    @Test
    fun `budgerigar 6 weeks applies 1_3 correction`() {
        // 6 недель = 42/365 ≈ 0.115 г.: 0.115·80/7·1.3 ≈ 1.7
        val result = calculator.toHumanYears(ageInYears = 42.0 / 365.0, type = BirdType.Budgerigar)
        assertEquals(1.71, result, 0.05)
    }

    @Test
    fun `correction applies just below 6 months but not at 6 months`() {
        val justBelow = calculator.toHumanYears(0.4999, BirdType.Budgerigar)
        val atThreshold = calculator.toHumanYears(0.5, BirdType.Budgerigar)
        // На пороге множитель отключается → скачок вниз ровно в 1.3 раза.
        assertEquals(justBelow / 1.3, atThreshold, 0.05)
    }

    @Test
    fun `throws on zero age`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(0.0, BirdType.Budgerigar)
        }
    }

    @Test
    fun `throws on negative age`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(-1.0, BirdType.Budgerigar)
        }
    }

    @Test
    fun `unified toHumanYears via params matches direct overload`() {
        val viaParams = calculator.toHumanYears(3.0, SpeciesParams.Bird(BirdType.Budgerigar))
        val viaOverload = calculator.toHumanYears(3.0, BirdType.Budgerigar)
        assertEquals(viaOverload, viaParams, 1e-9)
        assertEquals(34.29, viaParams, 0.05)
    }

    @Test
    fun `rejects non-Bird params`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(3.0, SpeciesParams.Rat)
        }
    }

    @ParameterizedTest(name = "{0} at {1} years = {2} human years")
    @CsvSource(
        "budgerigar, 1.0, 11.43",
        "budgerigar, 3.0, 34.29",
        "cockatiel, 5.0, 26.67",
        "canary, 2.0, 16.0",
        "macaw, 10.0, 16.0",
        "amazon, 10.0, 16.0",
        "conure, 4.0, 16.0",
    )
    fun `human years for table ages above 6 months`(
        typeId: String,
        birdAge: Double,
        expectedHuman: Double,
    ) {
        val type = BirdType.fromId(typeId)!!
        val result = calculator.toHumanYears(birdAge, type)
        assertEquals(expectedHuman, result, 0.05)
    }

    @Test
    fun `result is monotonically increasing in age for fixed type`() {
        var previous = Double.NEGATIVE_INFINITY
        for (ageHundredths in 1..3000) {
            val age = ageHundredths / 100.0
            val current = calculator.toHumanYears(age, BirdType.Macaw)
            // Поправка ×1.3 действует до 0.5 г. и создаёт намеренный скачок вниз ровно на стыке
            // (отключение множителя). Монотонность проверяется по обе стороны, кроме самого перехода.
            val crossesThreshold =
                age >= BirdAgeCalculator.JUVENILE_AGE_THRESHOLD &&
                    (age - 0.01) < BirdAgeCalculator.JUVENILE_AGE_THRESHOLD
            if (!crossesThreshold) {
                assert(current > previous) {
                    "Non-monotonic at age=$age: previous=$previous, current=$current"
                }
            }
            previous = current
        }
    }
}
