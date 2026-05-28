package app.pawclock.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlinx.serialization.json.Json

/**
 * JVM unit-тесты для [Route] sealed-иерархии (Task 17 / Plan 1).
 *
 * Проверяют:
 *  1. `data object` routes (PetsList/QuickCalculator/Settings/About) — singleton'ы (assertSame).
 *  2. `data class` routes (PetDetail/PetEditor) — корректные equals/hashCode/copy.
 *  3. PetEditor.petId nullable: default null = новый питомец.
 *  4. kotlinx.serialization: PetDetail и PetEditor правильно сериализуются и десериализуются
 *     — это критично для Navigation Compose 2.8 typesafe routing (см. KDoc на Route).
 */
class RouteTest {
    @Test
    fun `data object routes are singletons`() {
        assertSame(Route.PetsList, Route.PetsList)
        assertSame(Route.QuickCalculator, Route.QuickCalculator)
        assertSame(Route.Settings, Route.Settings)
        assertSame(Route.About, Route.About)
    }

    @Test
    fun `PetDetail equals and hashCode by petId`() {
        val a = Route.PetDetail(petId = 42L)
        val b = Route.PetDetail(petId = 42L)
        val c = Route.PetDetail(petId = 43L)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, c)
    }

    @Test
    fun `PetEditor defaults petId to null for new pet`() {
        val newPet = Route.PetEditor()
        assertNull(newPet.petId)
    }

    @Test
    fun `PetEditor differentiates new pet from edit pet`() {
        val newPet = Route.PetEditor()
        val editPet = Route.PetEditor(petId = 7L)
        assertNotEquals(newPet, editPet)
    }

    @Test
    fun `PetDetail round-trip via kotlinx serialization`() {
        val original = Route.PetDetail(petId = 123L)
        val encoded = Json.encodeToString(Route.PetDetail.serializer(), original)
        val decoded = Json.decodeFromString(Route.PetDetail.serializer(), encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun `PetEditor round-trip via kotlinx serialization with null id`() {
        val original = Route.PetEditor(petId = null)
        val encoded = Json.encodeToString(Route.PetEditor.serializer(), original)
        val decoded = Json.decodeFromString(Route.PetEditor.serializer(), encoded)
        assertEquals(original, decoded)
        assertNull(decoded.petId)
    }

    @Test
    fun `PetEditor round-trip via kotlinx serialization with non-null id`() {
        val original = Route.PetEditor(petId = 9999L)
        val encoded = Json.encodeToString(Route.PetEditor.serializer(), original)
        val decoded = Json.decodeFromString(Route.PetEditor.serializer(), encoded)
        assertEquals(original, decoded)
        assertEquals(9999L, decoded.petId)
    }
}
