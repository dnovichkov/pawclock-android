package app.pawclock.calculator

import app.pawclock.model.CalculationMethod
import app.pawclock.model.CatType
import app.pawclock.model.DogSize
import app.pawclock.model.Species
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Тесты sealed-интерфейса [AgeCalculator] и фабрики [AgeCalculator.forSpecies] (Plan 2, Task 1).
 *
 * Проверяют, что:
 *  1. `forSpecies(Dog/Cat)` возвращает ровно соответствующий `data object` (Strategy pattern).
 *  2. `forSpecies` для ещё не реализованных видов возвращает `null` (а не бросает) —
 *     это контракт, на который опирается [app.pawclock... CalculatePetAgeUseCase], бросая
 *     `UnsupportedSpeciesException` при `null`.
 *  3. Унифицированный метод `toHumanYears(age, params)` корректно делегирует в существующие
 *     перегрузки Dog/Cat и сохраняет числовые значения Plan 1.
 *  4. Передача чужого [SpeciesParams] в калькулятор — программерская ошибка (IllegalArgumentException).
 */
class AgeCalculatorTest {
    // ─── forSpecies factory invariants ─────────────────────────────────────

    @Test
    fun `forSpecies Dog returns the DogAgeCalculator data object`() {
        assertSame(DogAgeCalculator, AgeCalculator.forSpecies(Species.Dog))
    }

    @Test
    fun `forSpecies Cat returns the CatAgeCalculator data object`() {
        assertSame(CatAgeCalculator, AgeCalculator.forSpecies(Species.Cat))
    }

    @Test
    fun `forSpecies returns null for not-yet-implemented species`() {
        assertNull(AgeCalculator.forSpecies(Species.Rabbit))
        assertNull(AgeCalculator.forSpecies(Species.Bird))
        assertNull(AgeCalculator.forSpecies(Species.Horse))
    }

    @Test
    fun `forSpecies returns non-null exactly for implemented species`() {
        Species.all().forEach { species ->
            val calculator = AgeCalculator.forSpecies(species)
            if (species.isImplemented) {
                assertTrue(calculator != null, "expected calculator for implemented $species")
                assertEquals(species, calculator.species)
            } else {
                assertNull(calculator, "expected null for unimplemented $species")
            }
        }
    }

    // ─── unified toHumanYears delegation ───────────────────────────────────

    @Test
    fun `Dog calculator toHumanYears via params matches EPIGENETIC overload`() {
        val params = SpeciesParams.Dog(method = CalculationMethod.EPIGENETIC, size = DogSize.Medium)
        val viaParams = DogAgeCalculator.toHumanYears(5.0, params)
        val viaOverload = DogAgeCalculator.toHumanYears(5.0, CalculationMethod.EPIGENETIC, DogSize.Medium)
        assertEquals(viaOverload, viaParams, absoluteTolerance = 1e-9)
        assertEquals(56.7, viaParams, absoluteTolerance = 0.2)
    }

    @Test
    fun `Dog calculator toHumanYears via params honours SIZE_BASED method`() {
        val params = SpeciesParams.Dog(method = CalculationMethod.SIZE_BASED, size = DogSize.Medium)
        val viaParams = DogAgeCalculator.toHumanYears(5.0, params)
        assertEquals(36.0, viaParams, absoluteTolerance = 0.5)
    }

    @Test
    fun `Cat calculator toHumanYears via params matches catType overload`() {
        val params = SpeciesParams.Cat(type = CatType.IndoorShortHair)
        val viaParams = CatAgeCalculator.toHumanYears(5.0, params)
        val viaOverload = CatAgeCalculator.toHumanYears(5.0, CatType.IndoorShortHair)
        assertEquals(viaOverload, viaParams, absoluteTolerance = 1e-9)
        assertEquals(36.0, viaParams, absoluteTolerance = 0.01)
    }

    @Test
    fun `Dog calculator rejects Cat params`() {
        assertFailsWith<IllegalArgumentException> {
            DogAgeCalculator.toHumanYears(5.0, SpeciesParams.Cat(CatType.IndoorShortHair))
        }
    }

    @Test
    fun `Cat calculator rejects Dog params`() {
        assertFailsWith<IllegalArgumentException> {
            CatAgeCalculator.toHumanYears(5.0, SpeciesParams.Dog(CalculationMethod.EPIGENETIC, DogSize.Medium))
        }
    }
}
