package app.pawclock.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Финальная приёмка MVP v1.0 на уровне модели (Plan 2, Task 24).
 *
 * Закрепляет инвариант §12.1: к завершению Plan 2 реализованы **ровно 12** видов животных.
 * Это последний гейт, фиксирующий «полноту» списка видов — USP проекта «самый широкий список
 * среди бесплатных Android-калькуляторов» (§1.3). Тест перечислителен (а не рандомизирован):
 * домен `Species.all()` конечен, поэтому `forEach` покрывает всё пространство детерминированно.
 */
class Mvp1SpeciesAcceptanceTest {
    @Test
    fun `exactly twelve species are flagged implemented`() {
        val implementedCount = Species.all().count { it.isImplemented }
        assertEquals(12, implementedCount, "MVP v1.0 требует 12 реализованных видов (§12.1)")
    }

    @Test
    fun `every defined species is implemented after Plan 2`() {
        Species.all().forEach { species ->
            assertTrue(species.isImplemented, "${species.id} должен быть реализован к концу Plan 2")
        }
    }

    @Test
    fun `implemented set equals the full species set`() {
        assertEquals(Species.all().toSet(), Species.implemented().toSet())
    }
}
