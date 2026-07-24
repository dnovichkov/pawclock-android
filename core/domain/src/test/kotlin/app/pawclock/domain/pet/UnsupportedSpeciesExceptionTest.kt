package app.pawclock.domain.pet

import app.pawclock.model.Species
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Тесты [UnsupportedSpeciesException].
 *
 * После Plan 2 Task 10 реализованы все 12 видов, поэтому исключение больше не бросается через
 * публичный API UseCase'ов (защитные ветки `?: throw` / `if (!isImplemented) throw` стали
 * недостижимы). Прямой тест сохраняет покрытие класса и фиксирует контракт `species`/message —
 * на случай добавления нового вида-stub в будущем.
 */
class UnsupportedSpeciesExceptionTest {
    @Test
    fun `exposes the offending species`() {
        val ex = UnsupportedSpeciesException(Species.Fish)
        assertEquals(Species.Fish, ex.species)
    }

    @Test
    fun `message mentions the species id`() {
        val ex = UnsupportedSpeciesException(Species.Dog)
        assertTrue(ex.message!!.contains("dog"), "message should mention species id, was: ${ex.message}")
    }
}
