package app.pawclock.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ReptileTypeTest {
    @Test
    fun `all eight reptile types are defined`() {
        val types = ReptileType.entries
        assertEquals(8, types.size)
        assertTrue(types.contains(ReptileType.BoxTurtle))
        assertTrue(types.contains(ReptileType.RedEaredSlider))
        assertTrue(types.contains(ReptileType.BeardedDragon))
        assertTrue(types.contains(ReptileType.BallPython))
        assertTrue(types.contains(ReptileType.CornSnake))
        assertTrue(types.contains(ReptileType.GreenIguana))
        assertTrue(types.contains(ReptileType.LeopardGecko))
        assertTrue(types.contains(ReptileType.CrestedGecko))
    }

    @Test
    fun `each reptile type has stable id`() {
        assertEquals("box_turtle", ReptileType.BoxTurtle.id)
        assertEquals("red_eared_slider", ReptileType.RedEaredSlider.id)
        assertEquals("bearded_dragon", ReptileType.BeardedDragon.id)
        assertEquals("ball_python", ReptileType.BallPython.id)
        assertEquals("corn_snake", ReptileType.CornSnake.id)
        assertEquals("green_iguana", ReptileType.GreenIguana.id)
        assertEquals("leopard_gecko", ReptileType.LeopardGecko.id)
        assertEquals("crested_gecko", ReptileType.CrestedGecko.id)
    }

    @Test
    fun `fromId round-trip works`() {
        ReptileType.entries.forEach { type ->
            assertEquals(type, ReptileType.fromId(type.id))
        }
        assertNull(ReptileType.fromId("unknown"))
    }

    @Test
    fun `reference lifespans match section 4_9`() {
        assertEquals(40.0, ReptileType.BoxTurtle.averageLifespanYears, 0.001)
        assertEquals(20.0, ReptileType.CornSnake.averageLifespanYears, 0.001)
        assertEquals(20.0, ReptileType.GreenIguana.averageLifespanYears, 0.001)
        assertEquals(15.0, ReptileType.LeopardGecko.averageLifespanYears, 0.001)
    }

    @Test
    fun `all average lifespans are positive`() {
        ReptileType.entries.forEach { type ->
            assertTrue(
                type.averageLifespanYears > 0,
                "${type.id} average lifespan must be positive",
            )
        }
    }
}
