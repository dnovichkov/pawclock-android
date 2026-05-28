package app.pawclock.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DogSizeTest {
    @Test
    fun `all five sizes are defined`() {
        val sizes = DogSize.entries
        assertEquals(5, sizes.size)
        assertTrue(sizes.contains(DogSize.Toy))
        assertTrue(sizes.contains(DogSize.Small))
        assertTrue(sizes.contains(DogSize.Medium))
        assertTrue(sizes.contains(DogSize.Large))
        assertTrue(sizes.contains(DogSize.Giant))
    }

    @Test
    fun `weight ranges follow McMillan 2024 boundaries`() {
        // McMillan: малые <9 кг, средние 9–23, крупные 23–45, гигантские >45
        // Toy ≤4 кг (стандартная градация), Small 4-9 кг
        assertEquals(0.0, DogSize.Toy.minKg)
        assertEquals(4.0, DogSize.Toy.maxKg)
        assertEquals(4.0, DogSize.Small.minKg)
        assertEquals(9.0, DogSize.Small.maxKg)
        assertEquals(9.0, DogSize.Medium.minKg)
        assertEquals(23.0, DogSize.Medium.maxKg)
        assertEquals(23.0, DogSize.Large.minKg)
        assertEquals(45.0, DogSize.Large.maxKg)
        assertEquals(45.0, DogSize.Giant.minKg)
        assertNull(DogSize.Giant.maxKg)
    }

    @Test
    fun `fromWeight returns expected size for boundary values`() {
        assertEquals(DogSize.Toy, DogSize.fromWeight(2.0))
        assertEquals(DogSize.Toy, DogSize.fromWeight(3.9))
        assertEquals(DogSize.Small, DogSize.fromWeight(4.0))
        assertEquals(DogSize.Small, DogSize.fromWeight(8.9))
        assertEquals(DogSize.Medium, DogSize.fromWeight(9.0))
        assertEquals(DogSize.Medium, DogSize.fromWeight(22.9))
        assertEquals(DogSize.Large, DogSize.fromWeight(23.0))
        assertEquals(DogSize.Large, DogSize.fromWeight(44.9))
        assertEquals(DogSize.Giant, DogSize.fromWeight(45.0))
        assertEquals(DogSize.Giant, DogSize.fromWeight(80.0))
    }

    @Test
    fun `fromWeight returns null for non-positive weight`() {
        assertNull(DogSize.fromWeight(0.0))
        assertNull(DogSize.fromWeight(-1.0))
    }

    @Test
    fun `each size has stable id`() {
        assertEquals("toy", DogSize.Toy.id)
        assertEquals("small", DogSize.Small.id)
        assertEquals("medium", DogSize.Medium.id)
        assertEquals("large", DogSize.Large.id)
        assertEquals("giant", DogSize.Giant.id)
    }

    @Test
    fun `fromId round-trip works`() {
        DogSize.entries.forEach { size ->
            assertEquals(size, DogSize.fromId(size.id))
        }
        assertNull(DogSize.fromId("unknown"))
    }
}
