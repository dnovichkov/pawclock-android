package app.pawclock.calculator

import app.pawclock.model.ReptileType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * TDD-тесты скалярной формулы рептилии (PetPlace + Reptile Centre + A-Z Animals, §4.9 спецификации).
 *
 * Формула `ЧГ = age · 80 / lifespan` (общий [ScalarRatioFormula]), без поправки молодняка
 * (в отличие от птицы §4.8).
 *
 * Эталонные значения (§4.9):
 *  - Black/box turtle (ЧЖ 40): 5 л. = 10 ЧГ
 *  - Corn snake (ЧЖ 20): 3 г. = 12
 *  - Green iguana (ЧЖ 20): 5 л. = 20
 *  - Leopard gecko (ЧЖ 15): 2 г. ≈ 10.7
 */
class ReptileAgeCalculatorTest {
    private val calculator = ReptileAgeCalculator

    @Test
    fun `box turtle 5 years equals 10 human years`() {
        // 5·80/40 = 10
        val result = calculator.toHumanYears(ageInYears = 5.0, type = ReptileType.BoxTurtle)
        assertEquals(10.0, result, 0.01)
    }

    @Test
    fun `corn snake 3 years equals 12 human years`() {
        // 3·80/20 = 12
        val result = calculator.toHumanYears(ageInYears = 3.0, type = ReptileType.CornSnake)
        assertEquals(12.0, result, 0.01)
    }

    @Test
    fun `green iguana 5 years equals 20 human years`() {
        // 5·80/20 = 20
        val result = calculator.toHumanYears(ageInYears = 5.0, type = ReptileType.GreenIguana)
        assertEquals(20.0, result, 0.01)
    }

    @Test
    fun `leopard gecko 2 years equals 10_7 human years`() {
        // 2·80/15 ≈ 10.67
        val result = calculator.toHumanYears(ageInYears = 2.0, type = ReptileType.LeopardGecko)
        assertEquals(10.67, result, 0.05)
    }

    @Test
    fun `throws on zero age`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(0.0, ReptileType.GreenIguana)
        }
    }

    @Test
    fun `throws on negative age`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(-1.0, ReptileType.GreenIguana)
        }
    }

    @Test
    fun `unified toHumanYears via params matches direct overload`() {
        val viaParams = calculator.toHumanYears(3.0, SpeciesParams.Reptile(ReptileType.CornSnake))
        val viaOverload = calculator.toHumanYears(3.0, ReptileType.CornSnake)
        assertEquals(viaOverload, viaParams, 1e-9)
        assertEquals(12.0, viaParams, 0.01)
    }

    @Test
    fun `rejects non-Reptile params`() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.toHumanYears(3.0, SpeciesParams.Rat)
        }
    }

    @ParameterizedTest(name = "{0} at {1} years = {2} human years")
    @CsvSource(
        "box_turtle, 5.0, 10.0",
        "red_eared_slider, 5.0, 13.333",
        "corn_snake, 3.0, 12.0",
        "green_iguana, 5.0, 20.0",
        "leopard_gecko, 2.0, 10.667",
        "ball_python, 10.0, 26.667",
        "bearded_dragon, 6.0, 40.0",
    )
    fun `human years for table ages`(
        typeId: String,
        reptileAge: Double,
        expectedHuman: Double,
    ) {
        val type = ReptileType.fromId(typeId)!!
        val result = calculator.toHumanYears(reptileAge, type)
        assertEquals(expectedHuman, result, 0.05)
    }

    @Test
    fun `result is monotonically increasing in age for fixed type`() {
        var previous = Double.NEGATIVE_INFINITY
        for (ageHundredths in 1..4000) {
            val age = ageHundredths / 100.0
            val current = calculator.toHumanYears(age, ReptileType.BoxTurtle)
            assert(current > previous) {
                "Non-monotonic at age=$age: previous=$previous, current=$current"
            }
            previous = current
        }
    }

    @Test
    fun `ratio to lifespan invariant holds`() {
        // По определению скалярной формулы compute(age, lifespan)/age == 80/lifespan.
        val iguana = calculator.toHumanYears(7.0, ReptileType.GreenIguana)
        assertEquals(80.0 / 20.0, iguana / 7.0, 1e-9)
    }
}
