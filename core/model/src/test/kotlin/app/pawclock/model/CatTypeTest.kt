package app.pawclock.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CatTypeTest {
    @Test
    fun `all four cat types are defined`() {
        val types = CatType.entries
        assertEquals(4, types.size)
        assertTrue(types.contains(CatType.IndoorShortHair))
        assertTrue(types.contains(CatType.IndoorLongHair))
        assertTrue(types.contains(CatType.Outdoor))
        assertTrue(types.contains(CatType.LargeBreed))
    }

    @Test
    fun `Outdoor flag is set for Outdoor type only`() {
        assertTrue(CatType.Outdoor.isOutdoor)
        assertFalse(CatType.IndoorShortHair.isOutdoor)
        assertFalse(CatType.IndoorLongHair.isOutdoor)
        assertFalse(CatType.LargeBreed.isOutdoor)
    }

    @Test
    fun `LargeBreed flag is set for LargeBreed type only`() {
        assertTrue(CatType.LargeBreed.isLargeBreed)
        assertFalse(CatType.IndoorShortHair.isLargeBreed)
        assertFalse(CatType.IndoorLongHair.isLargeBreed)
        assertFalse(CatType.Outdoor.isLargeBreed)
    }

    @Test
    fun `each cat type has stable id`() {
        assertEquals("indoor_short_hair", CatType.IndoorShortHair.id)
        assertEquals("indoor_long_hair", CatType.IndoorLongHair.id)
        assertEquals("outdoor", CatType.Outdoor.id)
        assertEquals("large_breed", CatType.LargeBreed.id)
    }

    @Test
    fun `fromId round-trip works`() {
        CatType.entries.forEach { type ->
            assertEquals(type, CatType.fromId(type.id))
        }
        assertNull(CatType.fromId("unknown"))
    }
}
