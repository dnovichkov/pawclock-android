package app.pawclock.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GenderTest {
    @Test
    fun `all three genders are defined`() {
        val genders = Gender.entries
        assertEquals(3, genders.size)
        assertTrue(genders.contains(Gender.Male))
        assertTrue(genders.contains(Gender.Female))
        assertTrue(genders.contains(Gender.Unknown))
    }

    @Test
    fun `each gender has stable id`() {
        assertEquals("male", Gender.Male.id)
        assertEquals("female", Gender.Female.id)
        assertEquals("unknown", Gender.Unknown.id)
    }

    @Test
    fun `fromId round-trip works`() {
        Gender.entries.forEach { gender ->
            assertEquals(gender, Gender.fromId(gender.id))
        }
        assertNull(Gender.fromId("nonsense"))
    }
}
