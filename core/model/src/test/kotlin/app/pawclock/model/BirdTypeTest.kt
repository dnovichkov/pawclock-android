package app.pawclock.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BirdTypeTest {
    @Test
    fun `all ten bird types are defined`() {
        val types = BirdType.entries
        assertEquals(10, types.size)
        assertTrue(types.contains(BirdType.Budgerigar))
        assertTrue(types.contains(BirdType.Cockatiel))
        assertTrue(types.contains(BirdType.Canary))
        assertTrue(types.contains(BirdType.Lovebird))
        assertTrue(types.contains(BirdType.Conure))
        assertTrue(types.contains(BirdType.Amazon))
        assertTrue(types.contains(BirdType.AfricanGrey))
        assertTrue(types.contains(BirdType.Cockatoo))
        assertTrue(types.contains(BirdType.Macaw))
        assertTrue(types.contains(BirdType.Pigeon))
    }

    @Test
    fun `each bird type has stable id`() {
        assertEquals("budgerigar", BirdType.Budgerigar.id)
        assertEquals("cockatiel", BirdType.Cockatiel.id)
        assertEquals("canary", BirdType.Canary.id)
        assertEquals("lovebird", BirdType.Lovebird.id)
        assertEquals("conure", BirdType.Conure.id)
        assertEquals("amazon", BirdType.Amazon.id)
        assertEquals("african_grey", BirdType.AfricanGrey.id)
        assertEquals("cockatoo", BirdType.Cockatoo.id)
        assertEquals("macaw", BirdType.Macaw.id)
        assertEquals("pigeon", BirdType.Pigeon.id)
    }

    @Test
    fun `fromId round-trip works`() {
        BirdType.entries.forEach { type ->
            assertEquals(type, BirdType.fromId(type.id))
        }
        assertNull(BirdType.fromId("unknown"))
    }

    @Test
    fun `reference lifespans match section 4_8`() {
        assertEquals(7.0, BirdType.Budgerigar.averageLifespanYears, 0.001)
        assertEquals(15.0, BirdType.Cockatiel.averageLifespanYears, 0.001)
        assertEquals(10.0, BirdType.Canary.averageLifespanYears, 0.001)
        assertEquals(50.0, BirdType.Macaw.averageLifespanYears, 0.001)
        assertEquals(50.0, BirdType.Amazon.averageLifespanYears, 0.001)
    }

    @Test
    fun `all average lifespans are positive`() {
        BirdType.entries.forEach { type ->
            assertTrue(
                type.averageLifespanYears > 0,
                "${type.id} average lifespan must be positive",
            )
        }
    }
}
