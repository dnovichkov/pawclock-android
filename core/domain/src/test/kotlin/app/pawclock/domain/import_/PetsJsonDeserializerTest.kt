package app.pawclock.domain.import_

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Тесты для [PetsJsonDeserializer] (Plan 2, Task 19).
 *
 * Десериализатор — обратная сторона [app.pawclock.domain.export.PetsJsonSerializer]: он
 * переиспользует ту же схему ([app.pawclock.domain.export.PetsExportSchema] /
 * [app.pawclock.domain.export.PetExportEntry]), но добавляет fail-fast валидацию для
 * недоверенного ввода (импорт из произвольного файла, §3.5).
 *
 * Контракт: `decode` НЕ бросает на ожидаемых ошибках данных — возвращает
 * [PetsImportResult.Failure] с типизированным [ImportException]. Бросает (throw) уже
 * [ImportPetsUseCase] на свой страх (см. [ImportPetsUseCaseTest]).
 */
class PetsJsonDeserializerTest {
    private val validJson =
        """
        {
          "schema_version": 1,
          "exported_at": "2026-05-29T12:00:00Z",
          "pets": [
            {
              "name": "Рекс",
              "species_id": "dog",
              "subcategory_id": "medium",
              "birth_date": "2020-03-15",
              "gender_id": "male",
              "weight_kg": 25.5,
              "notes": "Любит мяч"
            }
          ]
        }
        """.trimIndent()

    @Test
    fun `parses valid export schema v1 into entries`() {
        val result = PetsJsonDeserializer.decode(validJson)

        val success = assertIs<PetsImportResult.Success>(result)
        assertEquals(1, success.entries.size)
        val entry = success.entries.first()
        assertEquals("Рекс", entry.name)
        assertEquals("dog", entry.speciesId)
        assertEquals("medium", entry.subcategoryId)
        assertEquals("2020-03-15", entry.birthDate)
        assertEquals("male", entry.genderId)
        assertEquals(25.5, entry.weightKg)
        assertEquals("Любит мяч", entry.notes)
        assertTrue(success.warnings.isEmpty(), "валидный ввод не даёт предупреждений")
    }

    @Test
    fun `accepts null optional fields`() {
        val json =
            """
            {"schema_version":1,"exported_at":"2026-05-29T12:00:00Z",
             "pets":[{"name":"Немо","species_id":"fish","birth_date":"2024-01-01"}]}
            """.trimIndent()

        val success = assertIs<PetsImportResult.Success>(PetsJsonDeserializer.decode(json))
        val entry = success.entries.first()
        assertNull(entry.subcategoryId)
        assertNull(entry.genderId)
        assertNull(entry.weightKg)
        assertNull(entry.notes)
    }

    @Test
    fun `fails when required field name is missing`() {
        val json =
            """
            {"schema_version":1,"exported_at":"x",
             "pets":[{"species_id":"dog","birth_date":"2020-01-01"}]}
            """.trimIndent()

        val failure = assertIs<PetsImportResult.Failure>(PetsJsonDeserializer.decode(json))
        assertIs<ImportException.MissingRequiredField>(failure.error)
    }

    @Test
    fun `fails when required field species_id is missing`() {
        val json =
            """
            {"schema_version":1,"exported_at":"x",
             "pets":[{"name":"Рекс","birth_date":"2020-01-01"}]}
            """.trimIndent()

        val failure = assertIs<PetsImportResult.Failure>(PetsJsonDeserializer.decode(json))
        assertIs<ImportException.MissingRequiredField>(failure.error)
    }

    @Test
    fun `fails when required field birth_date is missing`() {
        val json =
            """
            {"schema_version":1,"exported_at":"x",
             "pets":[{"name":"Рекс","species_id":"dog"}]}
            """.trimIndent()

        val failure = assertIs<PetsImportResult.Failure>(PetsJsonDeserializer.decode(json))
        assertIs<ImportException.MissingRequiredField>(failure.error)
    }

    @Test
    fun `fails fast on unknown species_id`() {
        val json =
            """
            {"schema_version":1,"exported_at":"x",
             "pets":[{"name":"Дракоша","species_id":"dragon","birth_date":"2020-01-01"}]}
            """.trimIndent()

        val failure = assertIs<PetsImportResult.Failure>(PetsJsonDeserializer.decode(json))
        val error = assertIs<ImportException.UnknownSpecies>(failure.error)
        assertEquals("dragon", error.speciesId)
    }

    @Test
    fun `fails on malformed JSON`() {
        val failure = assertIs<PetsImportResult.Failure>(PetsJsonDeserializer.decode("{ not json"))
        assertIs<ImportException.MalformedData>(failure.error)
    }

    @Test
    fun `fails on empty input`() {
        val failure = assertIs<PetsImportResult.Failure>(PetsJsonDeserializer.decode(""))
        assertIs<ImportException.MalformedData>(failure.error)
    }

    @Test
    fun `fails forward-incompatible schema version greater than current`() {
        val json =
            """
            {"schema_version":2,"exported_at":"x","pets":[]}
            """.trimIndent()

        val failure = assertIs<PetsImportResult.Failure>(PetsJsonDeserializer.decode(json))
        val error = assertIs<ImportException.UnsupportedSchemaVersion>(failure.error)
        assertEquals(2, error.version)
        assertEquals(1, error.supported)
    }

    @Test
    fun `fails on invalid birth_date format`() {
        val json =
            """
            {"schema_version":1,"exported_at":"x",
             "pets":[{"name":"Рекс","species_id":"dog","birth_date":"15-03-2020"}]}
            """.trimIndent()

        val failure = assertIs<PetsImportResult.Failure>(PetsJsonDeserializer.decode(json))
        assertIs<ImportException.MalformedData>(failure.error)
    }

    @Test
    fun `fails on blank name`() {
        val json =
            """
            {"schema_version":1,"exported_at":"x",
             "pets":[{"name":"   ","species_id":"dog","birth_date":"2020-01-01"}]}
            """.trimIndent()

        val failure = assertIs<PetsImportResult.Failure>(PetsJsonDeserializer.decode(json))
        assertIs<ImportException.MalformedData>(failure.error)
    }

    @Test
    fun `unknown gender_id is a lenient warning not a failure`() {
        val json =
            """
            {"schema_version":1,"exported_at":"x",
             "pets":[{"name":"Рекс","species_id":"dog","birth_date":"2020-01-01","gender_id":"neuter"}]}
            """.trimIndent()

        val success = assertIs<PetsImportResult.Success>(PetsJsonDeserializer.decode(json))
        assertEquals(1, success.entries.size)
        assertEquals(1, success.warnings.size)
        val warning = assertIs<ImportWarning.UnknownGender>(success.warnings.first())
        assertEquals("neuter", warning.genderId)
        assertEquals("Рекс", warning.petName)
    }

    @Test
    fun `schema version absent defaults to current and parses`() {
        val json =
            """
            {"exported_at":"x","pets":[{"name":"Рекс","species_id":"dog","birth_date":"2020-01-01"}]}
            """.trimIndent()

        assertIs<PetsImportResult.Success>(PetsJsonDeserializer.decode(json))
    }
}
