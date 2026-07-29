package app.pawclock.calculator

import app.pawclock.model.Species
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Финальная приёмка MVP v1.0 на уровне калькуляторов (Plan 2, Task 24).
 *
 * Закрепляет, что фабрики Strategy-pattern ([AgeCalculator.forSpecies] / [LifeStageCalculator.forSpecies])
 * возвращают не-`null` реализацию для **каждого** вида — то есть каждый из 12 видов умеет считать и
 * возраст, и стадию жизни. Это «exhaustive test» из чек-листа Task 24: домен конечен, поэтому
 * `forEach` по `Species.all()` сильнее рандомизированной выборки.
 *
 * Дополнительно проверяется самосогласованность: `forSpecies(s).species == s` (фабрика не путает
 * виды) — баг здесь молча подсунул бы чужую формулу.
 */
class Mvp1CalculatorAcceptanceTest {
    @Test
    fun `every species has a non-null age calculator bound to itself`() {
        Species.all().forEach { species ->
            val calculator = AgeCalculator.forSpecies(species)
            assertNotNull(calculator, "нет AgeCalculator для $species")
            assertEquals(species, calculator.species, "AgeCalculator.forSpecies($species) привязан к чужому виду")
        }
    }

    @Test
    fun `every species has a non-null life-stage calculator bound to itself`() {
        Species.all().forEach { species ->
            val calculator = LifeStageCalculator.forSpecies(species)
            assertNotNull(calculator, "нет LifeStageCalculator для $species")
            assertEquals(species, calculator.species, "LifeStageCalculator.forSpecies($species) привязан к чужому виду")
        }
    }
}
