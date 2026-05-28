package app.pawclock.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LifeStageTest {
    @Test
    fun `Dog life stages all exist`() {
        val stages = LifeStage.Dog.all()
        assertEquals(5, stages.size)
        assertTrue(stages.contains(LifeStage.Dog.Puppy))
        assertTrue(stages.contains(LifeStage.Dog.YoungAdult))
        assertTrue(stages.contains(LifeStage.Dog.MatureAdult))
        assertTrue(stages.contains(LifeStage.Dog.Senior))
        assertTrue(stages.contains(LifeStage.Dog.EndOfLife))
    }

    @Test
    fun `Cat life stages all exist`() {
        val stages = LifeStage.Cat.all()
        assertEquals(5, stages.size)
        assertTrue(stages.contains(LifeStage.Cat.Kitten))
        assertTrue(stages.contains(LifeStage.Cat.YoungAdult))
        assertTrue(stages.contains(LifeStage.Cat.MatureAdult))
        assertTrue(stages.contains(LifeStage.Cat.Senior))
        assertTrue(stages.contains(LifeStage.Cat.EndOfLife))
    }

    @Test
    fun `Dog Senior has displayKey dog_senior`() {
        assertEquals("dog_senior", LifeStage.Dog.Senior.displayKey)
    }

    @Test
    fun `Dog Puppy has displayKey dog_puppy`() {
        assertEquals("dog_puppy", LifeStage.Dog.Puppy.displayKey)
    }

    @Test
    fun `Cat Kitten has displayKey cat_kitten`() {
        assertEquals("cat_kitten", LifeStage.Cat.Kitten.displayKey)
    }

    @Test
    fun `Cat Senior has displayKey cat_senior`() {
        assertEquals("cat_senior", LifeStage.Cat.Senior.displayKey)
    }

    @Test
    fun `Dog and Cat stages with same name are distinct`() {
        val dogSenior: LifeStage = LifeStage.Dog.Senior
        val catSenior: LifeStage = LifeStage.Cat.Senior
        assertNotEquals(dogSenior, catSenior)
        val dogYa: LifeStage = LifeStage.Dog.YoungAdult
        val catYa: LifeStage = LifeStage.Cat.YoungAdult
        assertNotEquals(dogYa, catYa)
    }

    @Test
    fun `Dog stages have monotonic ordinals from Puppy to EndOfLife`() {
        val expectedOrder =
            listOf(
                LifeStage.Dog.Puppy,
                LifeStage.Dog.YoungAdult,
                LifeStage.Dog.MatureAdult,
                LifeStage.Dog.Senior,
                LifeStage.Dog.EndOfLife,
            )
        expectedOrder.zipWithNext().forEach { (a, b) ->
            assertTrue(a.ordinal < b.ordinal, "${a.displayKey}.ordinal should be < ${b.displayKey}.ordinal")
        }
    }

    @Test
    fun `Cat stages have monotonic ordinals from Kitten to EndOfLife`() {
        val expectedOrder =
            listOf(
                LifeStage.Cat.Kitten,
                LifeStage.Cat.YoungAdult,
                LifeStage.Cat.MatureAdult,
                LifeStage.Cat.Senior,
                LifeStage.Cat.EndOfLife,
            )
        expectedOrder.zipWithNext().forEach { (a, b) ->
            assertTrue(a.ordinal < b.ordinal, "${a.displayKey}.ordinal should be < ${b.displayKey}.ordinal")
        }
    }
}
