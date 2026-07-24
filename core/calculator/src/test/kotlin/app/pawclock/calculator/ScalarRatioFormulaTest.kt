package app.pawclock.calculator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

/**
 * Тесты общего helper'а [ScalarRatioFormula] (`ЧГ = age · 80 / lifespan`),
 * используемого Bird/Reptile/Fish (§4.8, §4.9, §4.11).
 */
class ScalarRatioFormulaTest {
    @Test
    fun `age equal to human reference lifespan gives one-to-one ratio`() {
        // age=1, lifespan=80 → 1·80/80 = 1
        assertEquals(1.0, ScalarRatioFormula.compute(age = 1.0, lifespan = 80.0), 1e-9)
    }

    @Test
    fun `scales linearly with age at reference lifespan`() {
        // age=10, lifespan=80 → 10
        assertEquals(10.0, ScalarRatioFormula.compute(age = 10.0, lifespan = 80.0), 1e-9)
    }

    @Test
    fun `shorter lifespan ages faster`() {
        // age=5, lifespan=40 → 5·80/40 = 10
        assertEquals(10.0, ScalarRatioFormula.compute(age = 5.0, lifespan = 40.0), 1e-9)
    }

    @Test
    fun `ratio to age equals 80 over lifespan`() {
        val lifespan = 7.0
        val ratio = ScalarRatioFormula.compute(age = 3.0, lifespan = lifespan) / 3.0
        assertEquals(ScalarRatioFormula.HUMAN_REFERENCE_LIFESPAN / lifespan, ratio, 1e-9)
    }

    @Test
    fun `throws on zero lifespan`() {
        assertThrows(IllegalArgumentException::class.java) {
            ScalarRatioFormula.compute(age = 1.0, lifespan = 0.0)
        }
    }

    @Test
    fun `throws on negative lifespan`() {
        assertThrows(IllegalArgumentException::class.java) {
            ScalarRatioFormula.compute(age = 1.0, lifespan = -5.0)
        }
    }
}
