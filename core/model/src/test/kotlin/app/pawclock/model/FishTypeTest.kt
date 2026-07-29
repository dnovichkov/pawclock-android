package app.pawclock.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FishTypeTest {
    @Test
    fun `all eight fish types are defined`() {
        val types = FishType.entries
        assertEquals(8, types.size)
        assertTrue(types.contains(FishType.Goldfish))
        assertTrue(types.contains(FishType.Koi))
        assertTrue(types.contains(FishType.Betta))
        assertTrue(types.contains(FishType.Guppy))
        assertTrue(types.contains(FishType.AngelFish))
        assertTrue(types.contains(FishType.NeonTetra))
        assertTrue(types.contains(FishType.Tropical))
        assertTrue(types.contains(FishType.Discus))
    }

    @Test
    fun `each fish type has stable id`() {
        assertEquals("goldfish", FishType.Goldfish.id)
        assertEquals("koi", FishType.Koi.id)
        assertEquals("betta", FishType.Betta.id)
        assertEquals("guppy", FishType.Guppy.id)
        assertEquals("angelfish", FishType.AngelFish.id)
        assertEquals("neon_tetra", FishType.NeonTetra.id)
        assertEquals("tropical", FishType.Tropical.id)
        assertEquals("discus", FishType.Discus.id)
    }

    @Test
    fun `fromId round-trip works`() {
        FishType.entries.forEach { type ->
            assertEquals(type, FishType.fromId(type.id))
        }
        assertNull(FishType.fromId("unknown"))
    }

    @Test
    fun `reference lifespans match section 4_11`() {
        assertEquals(15.0, FishType.Goldfish.averageLifespanYears, 0.001)
        assertEquals(30.0, FishType.Koi.averageLifespanYears, 0.001)
        assertEquals(5.0, FishType.Betta.averageLifespanYears, 0.001)
        assertEquals(2.0, FishType.Guppy.averageLifespanYears, 0.001)
        assertEquals(8.0, FishType.NeonTetra.averageLifespanYears, 0.001)
    }

    @Test
    fun `all average lifespans are positive`() {
        FishType.entries.forEach { type ->
            assertTrue(
                type.averageLifespanYears > 0,
                "${type.id} average lifespan must be positive",
            )
        }
    }
}
