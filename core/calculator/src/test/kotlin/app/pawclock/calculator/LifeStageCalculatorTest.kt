package app.pawclock.calculator

import app.pawclock.model.CatType
import app.pawclock.model.DogSize
import app.pawclock.model.LifeStage
import app.pawclock.model.CalculationMethod
import app.pawclock.model.Species
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Тесты sealed-интерфейса [LifeStageCalculator] и фабрики [LifeStageCalculator.forSpecies]
 * (Plan 2, Task 1).
 *
 * Аналогично [AgeCalculatorTest], но для калькуляторов стадии жизни.
 */
class LifeStageCalculatorTest {
    @Test
    fun `forSpecies Dog returns the DogLifeStageCalculator data object`() {
        assertSame(DogLifeStageCalculator, LifeStageCalculator.forSpecies(Species.Dog))
    }

    @Test
    fun `forSpecies Cat returns the CatLifeStageCalculator data object`() {
        assertSame(CatLifeStageCalculator, LifeStageCalculator.forSpecies(Species.Cat))
    }

    @Test
    fun `forSpecies returns the FishLifeStageCalculator data object`() {
        assertSame(FishLifeStageCalculator, LifeStageCalculator.forSpecies(Species.Fish))
    }

    @Test
    fun `forSpecies returns a non-null calculator for every species after Task 10`() {
        // После Task 10 реализованы все 12 видов — null больше не ожидается.
        Species.all().forEach { species ->
            val calculator = LifeStageCalculator.forSpecies(species)
            assertTrue(calculator != null, "expected calculator for $species")
            assertEquals(species, calculator.species)
        }
    }

    @Test
    fun `Dog calculator determine via params matches size overload`() {
        val params = SpeciesParams.Dog(method = CalculationMethod.EPIGENETIC, size = DogSize.Giant)
        val viaParams = DogLifeStageCalculator.determine(6.0, params)
        val viaOverload = DogLifeStageCalculator.determine(6.0, DogSize.Giant)
        assertSame(viaOverload, viaParams)
        assertEquals(LifeStage.Dog.Senior, viaParams)
    }

    @Test
    fun `Cat calculator determine via params matches catType overload`() {
        val params = SpeciesParams.Cat(type = CatType.IndoorShortHair)
        val viaParams = CatLifeStageCalculator.determine(12.0, params)
        val viaOverload = CatLifeStageCalculator.determine(12.0, CatType.IndoorShortHair)
        assertSame(viaOverload, viaParams)
        assertEquals(LifeStage.Cat.Senior, viaParams)
    }

    @Test
    fun `Dog life stage calculator rejects Cat params`() {
        assertFailsWith<IllegalArgumentException> {
            DogLifeStageCalculator.determine(6.0, SpeciesParams.Cat(CatType.IndoorShortHair))
        }
    }
}
