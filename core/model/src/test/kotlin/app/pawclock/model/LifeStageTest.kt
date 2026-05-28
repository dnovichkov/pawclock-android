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
    fun `Rabbit life stages all exist`() {
        val stages = LifeStage.Rabbit.all()
        assertEquals(5, stages.size)
        assertTrue(stages.contains(LifeStage.Rabbit.Infancy))
        assertTrue(stages.contains(LifeStage.Rabbit.Adolescence))
        assertTrue(stages.contains(LifeStage.Rabbit.YoungAdult))
        assertTrue(stages.contains(LifeStage.Rabbit.Adult))
        assertTrue(stages.contains(LifeStage.Rabbit.Senior))
    }

    @Test
    fun `Rabbit stages have stable displayKeys`() {
        assertEquals("rabbit_infancy", LifeStage.Rabbit.Infancy.displayKey)
        assertEquals("rabbit_adolescence", LifeStage.Rabbit.Adolescence.displayKey)
        assertEquals("rabbit_young_adult", LifeStage.Rabbit.YoungAdult.displayKey)
        assertEquals("rabbit_adult", LifeStage.Rabbit.Adult.displayKey)
        assertEquals("rabbit_senior", LifeStage.Rabbit.Senior.displayKey)
    }

    @Test
    fun `Rabbit stages have monotonic ordinals from Infancy to Senior`() {
        val expectedOrder =
            listOf(
                LifeStage.Rabbit.Infancy,
                LifeStage.Rabbit.Adolescence,
                LifeStage.Rabbit.YoungAdult,
                LifeStage.Rabbit.Adult,
                LifeStage.Rabbit.Senior,
            )
        expectedOrder.zipWithNext().forEach { (a, b) ->
            assertTrue(a.ordinal < b.ordinal, "${a.displayKey}.ordinal should be < ${b.displayKey}.ordinal")
        }
    }

    @Test
    fun `Hamster life stages all exist`() {
        val stages = LifeStage.Hamster.all()
        assertEquals(5, stages.size)
        assertTrue(stages.contains(LifeStage.Hamster.Pup))
        assertTrue(stages.contains(LifeStage.Hamster.Juvenile))
        assertTrue(stages.contains(LifeStage.Hamster.Adult))
        assertTrue(stages.contains(LifeStage.Hamster.Senior))
        assertTrue(stages.contains(LifeStage.Hamster.VerySenior))
    }

    @Test
    fun `Hamster stages have stable displayKeys`() {
        assertEquals("hamster_pup", LifeStage.Hamster.Pup.displayKey)
        assertEquals("hamster_juvenile", LifeStage.Hamster.Juvenile.displayKey)
        assertEquals("hamster_adult", LifeStage.Hamster.Adult.displayKey)
        assertEquals("hamster_senior", LifeStage.Hamster.Senior.displayKey)
        assertEquals("hamster_very_senior", LifeStage.Hamster.VerySenior.displayKey)
    }

    @Test
    fun `Hamster stages have monotonic ordinals from Pup to VerySenior`() {
        val expectedOrder =
            listOf(
                LifeStage.Hamster.Pup,
                LifeStage.Hamster.Juvenile,
                LifeStage.Hamster.Adult,
                LifeStage.Hamster.Senior,
                LifeStage.Hamster.VerySenior,
            )
        expectedOrder.zipWithNext().forEach { (a, b) ->
            assertTrue(a.ordinal < b.ordinal, "${a.displayKey}.ordinal should be < ${b.displayKey}.ordinal")
        }
    }

    @Test
    fun `GuineaPig life stages all exist`() {
        val stages = LifeStage.GuineaPig.all()
        assertEquals(5, stages.size)
        assertTrue(stages.contains(LifeStage.GuineaPig.Pup))
        assertTrue(stages.contains(LifeStage.GuineaPig.Juvenile))
        assertTrue(stages.contains(LifeStage.GuineaPig.Adult))
        assertTrue(stages.contains(LifeStage.GuineaPig.Senior))
        assertTrue(stages.contains(LifeStage.GuineaPig.Geriatric))
    }

    @Test
    fun `GuineaPig stages have stable displayKeys`() {
        assertEquals("guinea_pig_pup", LifeStage.GuineaPig.Pup.displayKey)
        assertEquals("guinea_pig_juvenile", LifeStage.GuineaPig.Juvenile.displayKey)
        assertEquals("guinea_pig_adult", LifeStage.GuineaPig.Adult.displayKey)
        assertEquals("guinea_pig_senior", LifeStage.GuineaPig.Senior.displayKey)
        assertEquals("guinea_pig_geriatric", LifeStage.GuineaPig.Geriatric.displayKey)
    }

    @Test
    fun `GuineaPig stages have monotonic ordinals from Pup to Geriatric`() {
        val expectedOrder =
            listOf(
                LifeStage.GuineaPig.Pup,
                LifeStage.GuineaPig.Juvenile,
                LifeStage.GuineaPig.Adult,
                LifeStage.GuineaPig.Senior,
                LifeStage.GuineaPig.Geriatric,
            )
        expectedOrder.zipWithNext().forEach { (a, b) ->
            assertTrue(a.ordinal < b.ordinal, "${a.displayKey}.ordinal should be < ${b.displayKey}.ordinal")
        }
    }

    @Test
    fun `Rat life stages all exist`() {
        val stages = LifeStage.Rat.all()
        assertEquals(5, stages.size)
        assertTrue(stages.contains(LifeStage.Rat.Pup))
        assertTrue(stages.contains(LifeStage.Rat.Juvenile))
        assertTrue(stages.contains(LifeStage.Rat.Adult))
        assertTrue(stages.contains(LifeStage.Rat.Senior))
        assertTrue(stages.contains(LifeStage.Rat.EndOfLife))
    }

    @Test
    fun `Rat stages have stable displayKeys`() {
        assertEquals("rat_pup", LifeStage.Rat.Pup.displayKey)
        assertEquals("rat_juvenile", LifeStage.Rat.Juvenile.displayKey)
        assertEquals("rat_adult", LifeStage.Rat.Adult.displayKey)
        assertEquals("rat_senior", LifeStage.Rat.Senior.displayKey)
        assertEquals("rat_end_of_life", LifeStage.Rat.EndOfLife.displayKey)
    }

    @Test
    fun `Rat stages have monotonic ordinals from Pup to EndOfLife`() {
        val expectedOrder =
            listOf(
                LifeStage.Rat.Pup,
                LifeStage.Rat.Juvenile,
                LifeStage.Rat.Adult,
                LifeStage.Rat.Senior,
                LifeStage.Rat.EndOfLife,
            )
        expectedOrder.zipWithNext().forEach { (a, b) ->
            assertTrue(a.ordinal < b.ordinal, "${a.displayKey}.ordinal should be < ${b.displayKey}.ordinal")
        }
    }

    @Test
    fun `Mouse life stages all exist`() {
        val stages = LifeStage.Mouse.all()
        assertEquals(5, stages.size)
        assertTrue(stages.contains(LifeStage.Mouse.Pup))
        assertTrue(stages.contains(LifeStage.Mouse.Juvenile))
        assertTrue(stages.contains(LifeStage.Mouse.Adult))
        assertTrue(stages.contains(LifeStage.Mouse.Senior))
        assertTrue(stages.contains(LifeStage.Mouse.EndOfLife))
    }

    @Test
    fun `Mouse stages have stable displayKeys`() {
        assertEquals("mouse_pup", LifeStage.Mouse.Pup.displayKey)
        assertEquals("mouse_juvenile", LifeStage.Mouse.Juvenile.displayKey)
        assertEquals("mouse_adult", LifeStage.Mouse.Adult.displayKey)
        assertEquals("mouse_senior", LifeStage.Mouse.Senior.displayKey)
        assertEquals("mouse_end_of_life", LifeStage.Mouse.EndOfLife.displayKey)
    }

    @Test
    fun `Mouse stages have monotonic ordinals from Pup to EndOfLife`() {
        val expectedOrder =
            listOf(
                LifeStage.Mouse.Pup,
                LifeStage.Mouse.Juvenile,
                LifeStage.Mouse.Adult,
                LifeStage.Mouse.Senior,
                LifeStage.Mouse.EndOfLife,
            )
        expectedOrder.zipWithNext().forEach { (a, b) ->
            assertTrue(a.ordinal < b.ordinal, "${a.displayKey}.ordinal should be < ${b.displayKey}.ordinal")
        }
    }

    @Test
    fun `Ferret life stages all exist`() {
        val stages = LifeStage.Ferret.all()
        assertEquals(5, stages.size)
        assertTrue(stages.contains(LifeStage.Ferret.Kit))
        assertTrue(stages.contains(LifeStage.Ferret.Juvenile))
        assertTrue(stages.contains(LifeStage.Ferret.Adult))
        assertTrue(stages.contains(LifeStage.Ferret.Senior))
        assertTrue(stages.contains(LifeStage.Ferret.Geriatric))
    }

    @Test
    fun `Ferret stages have stable displayKeys`() {
        assertEquals("ferret_kit", LifeStage.Ferret.Kit.displayKey)
        assertEquals("ferret_juvenile", LifeStage.Ferret.Juvenile.displayKey)
        assertEquals("ferret_adult", LifeStage.Ferret.Adult.displayKey)
        assertEquals("ferret_senior", LifeStage.Ferret.Senior.displayKey)
        assertEquals("ferret_geriatric", LifeStage.Ferret.Geriatric.displayKey)
    }

    @Test
    fun `Ferret stages have monotonic ordinals from Kit to Geriatric`() {
        val expectedOrder =
            listOf(
                LifeStage.Ferret.Kit,
                LifeStage.Ferret.Juvenile,
                LifeStage.Ferret.Adult,
                LifeStage.Ferret.Senior,
                LifeStage.Ferret.Geriatric,
            )
        expectedOrder.zipWithNext().forEach { (a, b) ->
            assertTrue(a.ordinal < b.ordinal, "${a.displayKey}.ordinal should be < ${b.displayKey}.ordinal")
        }
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
