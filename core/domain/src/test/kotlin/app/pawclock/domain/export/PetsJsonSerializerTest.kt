package app.pawclock.domain.export

import app.pawclock.model.Gender
import app.pawclock.model.Pet
import app.pawclock.model.Species
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Тесты для [PetsJsonSerializer] (Plan 2, Task 17).
 *
 * Проверяют контракт экспортного JSON: snake_case-имена полей, опускание null'ов,
 * стабильные `id` видов/пола/подкатегории, ISO-8601 даты и наличие `schema_version`
 * для forward-compatibility. Все проверки структурные (через decode/parseToJsonElement),
 * чтобы не зависеть от форматирования prettyPrint.
 */
class PetsJsonSerializerTest {
    private val exportedAt: Instant = Instant.parse("2026-05-29T12:00:00Z")

    private val rex =
        Pet(
            id = 1L,
            name = "Рекс",
            species = Species.Dog,
            birthDate = LocalDate.of(2020, 3, 15),
            subcategory = "medium",
            gender = Gender.Male,
            weightKg = 25.5,
            notes = "Любит мяч",
            photoPath = "/data/photos/rex.jpg",
        )

    private fun firstEntryObject(json: String) =
        Json
            .parseToJsonElement(json)
            .jsonObject["pets"]!!
            .jsonArray
            .first()
            .jsonObject

    @Test
    fun `serializes single Dog pet with all populated fields`() {
        val json = PetsJsonSerializer.encode(listOf(rex), exportedAt)
        val schema = Json.decodeFromString(PetsExportSchema.serializer(), json)

        assertEquals(1, schema.pets.size)
        val entry = schema.pets.first()
        assertEquals("Рекс", entry.name)
        assertEquals("dog", entry.speciesId)
        assertEquals("medium", entry.subcategoryId)
        assertEquals("2020-03-15", entry.birthDate)
        assertEquals("male", entry.genderId)
        assertEquals(25.5, entry.weightKg)
        assertEquals("Любит мяч", entry.notes)
    }

    @Test
    fun `does not export id or photoPath`() {
        val json = PetsJsonSerializer.encode(listOf(rex), exportedAt)

        assertFalse(json.contains("photoPath"), "photoPath не должен попадать в бэкап")
        assertFalse(json.contains("photo_path"), "photo_path не должен попадать в бэкап")
        assertFalse(json.contains("rex.jpg"), "путь к фото — локальный artifact, не экспортируется")
    }

    @Test
    fun `serializes list of three pets preserving subcategory types`() {
        val cat =
            Pet(
                id = 2L,
                name = "Мурка",
                species = Species.Cat,
                birthDate = LocalDate.of(2019, 1, 1),
                subcategory = "indoor_short_hair",
            )
        val rabbit =
            Pet(
                id = 3L,
                name = "Снежок",
                species = Species.Rabbit,
                birthDate = LocalDate.of(2022, 6, 1),
                subcategory = "dwarf",
            )
        val json = PetsJsonSerializer.encode(listOf(rex, cat, rabbit), exportedAt)
        val schema = Json.decodeFromString(PetsExportSchema.serializer(), json)

        assertEquals(listOf("dog", "cat", "rabbit"), schema.pets.map { it.speciesId })
        assertEquals(listOf("medium", "indoor_short_hair", "dwarf"), schema.pets.map { it.subcategoryId })
    }

    @Test
    fun `serialized JSON contains schema version 1`() {
        val json = PetsJsonSerializer.encode(emptyList(), exportedAt)
        val root = Json.parseToJsonElement(json).jsonObject

        assertEquals(1, root["schema_version"]?.jsonPrimitive?.int)
    }

    @Test
    fun `null optional fields are omitted not serialized as null`() {
        val minimal =
            Pet(
                id = 5L,
                name = "Немо",
                species = Species.Fish,
                birthDate = LocalDate.of(2024, 1, 1),
            )
        val json = PetsJsonSerializer.encode(listOf(minimal), exportedAt)
        val entry = firstEntryObject(json)

        assertFalse(entry.containsKey("subcategory_id"))
        assertFalse(entry.containsKey("gender_id"))
        assertFalse(entry.containsKey("weight_kg"))
        assertFalse(entry.containsKey("notes"))
        assertTrue(entry.containsKey("name"))
        assertTrue(entry.containsKey("species_id"))
        assertTrue(entry.containsKey("birth_date"))
        assertFalse(json.contains("null"), "null-значения не должны сериализоваться как литерал null")
    }

    @Test
    fun `birthDate is ISO-8601 date string`() {
        val json = PetsJsonSerializer.encode(listOf(rex), exportedAt)
        val schema = Json.decodeFromString(PetsExportSchema.serializer(), json)

        assertEquals(LocalDate.of(2020, 3, 15), LocalDate.parse(schema.pets.first().birthDate))
    }

    @Test
    fun `exportedAt is ISO-8601 instant string`() {
        val json = PetsJsonSerializer.encode(emptyList(), exportedAt)
        val schema = Json.decodeFromString(PetsExportSchema.serializer(), json)

        assertEquals(exportedAt, Instant.parse(schema.exportedAt))
    }

    @Test
    fun `species id is used not Species toString`() {
        val json = PetsJsonSerializer.encode(listOf(rex), exportedAt)
        val entry = firstEntryObject(json)

        // Species.Dog.toString() == "Dog" — экспорт обязан использовать стабильный id "dog"
        assertEquals("dog", entry["species_id"]?.jsonPrimitive?.content)
    }
}
