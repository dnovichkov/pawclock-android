package app.pawclock.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HorseTypeTest {
    @Test
    fun `all four horse types are defined`() {
        val types = HorseType.entries
        assertEquals(4, types.size)
        assertTrue(types.contains(HorseType.Pony))
        assertTrue(types.contains(HorseType.LightHorse))
        assertTrue(types.contains(HorseType.DraftHorse))
        assertTrue(types.contains(HorseType.Thoroughbred))
    }

    @Test
    fun `each horse type has stable id`() {
        assertEquals("pony", HorseType.Pony.id)
        assertEquals("light_horse", HorseType.LightHorse.id)
        assertEquals("draft_horse", HorseType.DraftHorse.id)
        assertEquals("thoroughbred", HorseType.Thoroughbred.id)
    }

    @Test
    fun `fromId round-trip works`() {
        HorseType.entries.forEach { type ->
            assertEquals(type, HorseType.fromId(type.id))
        }
        assertNull(HorseType.fromId("unknown"))
    }

    @Test
    fun `all average lifespans are positive`() {
        HorseType.entries.forEach { type ->
            assertTrue(
                type.averageLifespanYears > 0,
                "${type.id} average lifespan must be positive",
            )
        }
    }
}
