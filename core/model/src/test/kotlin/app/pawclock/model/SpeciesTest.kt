package app.pawclock.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SpeciesTest {
    @Test
    fun `fromString returns Dog for dog identifier`() {
        assertEquals(Species.Dog, Species.fromString("dog"))
    }

    @Test
    fun `fromString returns Cat for cat identifier`() {
        assertEquals(Species.Cat, Species.fromString("cat"))
    }

    @Test
    fun `fromString is case insensitive`() {
        assertEquals(Species.Dog, Species.fromString("DOG"))
        assertEquals(Species.Cat, Species.fromString("Cat"))
    }

    @Test
    fun `fromString returns null for unknown identifier`() {
        assertNull(Species.fromString("unicorn"))
        assertNull(Species.fromString(""))
    }

    @Test
    fun `all twelve species are defined`() {
        val all = Species.all()
        assertEquals(12, all.size)
        assertTrue(all.contains(Species.Dog))
        assertTrue(all.contains(Species.Cat))
        assertTrue(all.contains(Species.Rabbit))
        assertTrue(all.contains(Species.Hamster))
        assertTrue(all.contains(Species.GuineaPig))
        assertTrue(all.contains(Species.Rat))
        assertTrue(all.contains(Species.Mouse))
        assertTrue(all.contains(Species.Ferret))
        assertTrue(all.contains(Species.Bird))
        assertTrue(all.contains(Species.Reptile))
        assertTrue(all.contains(Species.Horse))
        assertTrue(all.contains(Species.Fish))
    }

    @Test
    fun `only Dog and Cat are implemented in this plan`() {
        assertTrue(Species.Dog.isImplemented)
        assertTrue(Species.Cat.isImplemented)
        assertFalse(Species.Rabbit.isImplemented)
        assertFalse(Species.Hamster.isImplemented)
        assertFalse(Species.GuineaPig.isImplemented)
        assertFalse(Species.Rat.isImplemented)
        assertFalse(Species.Mouse.isImplemented)
        assertFalse(Species.Ferret.isImplemented)
        assertFalse(Species.Bird.isImplemented)
        assertFalse(Species.Reptile.isImplemented)
        assertFalse(Species.Horse.isImplemented)
        assertFalse(Species.Fish.isImplemented)
    }

    @Test
    fun `implemented returns exactly Dog and Cat`() {
        val implemented = Species.implemented()
        assertEquals(2, implemented.size)
        assertTrue(implemented.contains(Species.Dog))
        assertTrue(implemented.contains(Species.Cat))
    }

    @Test
    fun `each species has a stable identifier`() {
        assertEquals("dog", Species.Dog.id)
        assertEquals("cat", Species.Cat.id)
        assertEquals("rabbit", Species.Rabbit.id)
        assertEquals("hamster", Species.Hamster.id)
        assertEquals("guinea_pig", Species.GuineaPig.id)
        assertEquals("rat", Species.Rat.id)
        assertEquals("mouse", Species.Mouse.id)
        assertEquals("ferret", Species.Ferret.id)
        assertEquals("bird", Species.Bird.id)
        assertEquals("reptile", Species.Reptile.id)
        assertEquals("horse", Species.Horse.id)
        assertEquals("fish", Species.Fish.id)
    }

    @Test
    fun `fromString round-trip works for every species id`() {
        Species.all().forEach { species ->
            assertEquals(species, Species.fromString(species.id), "Round-trip failed for ${species.id}")
            assertNotNull(Species.fromString(species.id))
        }
    }
}
