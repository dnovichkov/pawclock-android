package app.pawclock.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HamsterTypeTest {
    @Test
    fun `all five hamster types are defined`() {
        val types = HamsterType.entries
        assertEquals(5, types.size)
        assertTrue(types.contains(HamsterType.Syrian))
        assertTrue(types.contains(HamsterType.Dwarf))
        assertTrue(types.contains(HamsterType.Roborovski))
        assertTrue(types.contains(HamsterType.Chinese))
        assertTrue(types.contains(HamsterType.WinterWhite))
    }

    @Test
    fun `each hamster type has stable id`() {
        assertEquals("syrian", HamsterType.Syrian.id)
        assertEquals("dwarf", HamsterType.Dwarf.id)
        assertEquals("roborovski", HamsterType.Roborovski.id)
        assertEquals("chinese", HamsterType.Chinese.id)
        assertEquals("winter_white", HamsterType.WinterWhite.id)
    }

    @Test
    fun `fromId round-trip works`() {
        HamsterType.entries.forEach { type ->
            assertEquals(type, HamsterType.fromId(type.id))
        }
        assertNull(HamsterType.fromId("unknown"))
    }

    @Test
    fun `average lifespan is within species range 1_5 to 3_5 years`() {
        HamsterType.entries.forEach { type ->
            assertTrue(
                type.averageLifespanYears in 1.5..3.5,
                "${type.id} average lifespan ${type.averageLifespanYears} out of §4.4 range",
            )
        }
    }

    @Test
    fun `Roborovski has the longest average lifespan`() {
        val longest = HamsterType.entries.maxByOrNull { it.averageLifespanYears }
        assertEquals(HamsterType.Roborovski, longest)
    }
}
