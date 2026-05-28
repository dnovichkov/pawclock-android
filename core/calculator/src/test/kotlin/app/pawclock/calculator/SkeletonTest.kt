package app.pawclock.calculator

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Smoke test for the :core:calculator skeleton.
 *
 * Verifies that the JVM toolchain is wired up, kotlin-test resolves, and JUnit Platform
 * picks up tests. If this passes, the TDD pipeline for [DogAgeCalculator] /
 * [CatAgeCalculator] formulas (Tasks 6-10) is ready to use.
 */
class SkeletonTest {
    @Test
    fun `arithmetic works — JVM test pipeline is alive`() {
        assertEquals(2, 1 + 1)
    }
}
