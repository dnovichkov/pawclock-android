package app.pawclock.domain.import_

import app.pawclock.domain.export.PetsCsvSerializer
import app.pawclock.model.Gender
import app.pawclock.model.Pet
import app.pawclock.model.Species
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Тесты для [PetsCsvDeserializer] (Plan 2, Task 20).
 *
 * Десериализатор — обратная сторона [PetsCsvSerializer]: RFC 4180-парсер, восстанавливающий
 * [app.pawclock.domain.export.PetExportEntry] из табличного CSV (в т.ч. сгенерированного Excel).
 * Переиспользует ту же пост-валидацию записей, что и [PetsJsonDeserializer]
 * ([ImportEntryValidator]): fail-fast на пустом имени/неизвестном виде/плохой дате,
 * lenient warning на неизвестном поле.
 *
 * Контракт (как у JSON, см. [PetsJsonDeserializerTest]): `decode` НЕ бросает на ожидаемых
 * ошибках данных — возвращает [PetsImportResult.Failure] с типизированным [ImportException].
 */
class PetsCsvDeserializerTest {
    private val header = "name,species_id,subcategory_id,birth_date,gender_id,weight_kg,notes"

    /** Собирает CSV из заголовка и строк данных с CRLF-разделителем (как [PetsCsvSerializer]). */
    private fun csv(vararg rows: String): String = (listOf(header) + rows).joinToString("\r\n")

    @Test
    fun `parses valid CSV with header row into entries`() {
        val input = csv("Рекс,dog,medium,2020-03-15,male,25.5,Любит мяч")

        val success = assertIs<PetsImportResult.Success>(PetsCsvDeserializer.decode(input))
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
    fun `handles quoted fields with commas and newlines (RFC 4180)`() {
        // Round-trip через сериализатор: имя с запятой+кавычками, заметки с переносом строки.
        val tricky =
            Pet(
                id = 7L,
                name = "Рекс, \"Большой\"",
                species = Species.Dog,
                birthDate = LocalDate.of(2020, 3, 15),
                subcategory = "large",
                gender = Gender.Male,
                weightKg = 30.0,
                notes = "первая\nвторая,третья",
            )
        val input = PetsCsvSerializer.encode(listOf(tricky), Instant.parse("2026-05-29T12:00:00Z"))

        val success = assertIs<PetsImportResult.Success>(PetsCsvDeserializer.decode(input))
        val entry = success.entries.single()
        assertEquals("Рекс, \"Большой\"", entry.name, "запятая и удвоенные кавычки восстановлены")
        assertEquals("первая\nвторая,третья", entry.notes, "перенос строки и запятая в поле сохранены")
        assertEquals(30.0, entry.weightKg)
    }

    @Test
    fun `fails on missing required column`() {
        val noSpecies = "name,subcategory_id,birth_date\r\nРекс,medium,2020-03-15"

        val failure = assertIs<PetsImportResult.Failure>(PetsCsvDeserializer.decode(noSpecies))
        val error = assertIs<ImportException.MissingRequiredField>(failure.error)
        assertTrue("species_id" in error.fields, "недостающая обязательная колонка названа")
    }

    @Test
    fun `fails fast on row with unknown species_id`() {
        val input = csv("Дракоша,dragon,,2020-01-01,,,")

        val failure = assertIs<PetsImportResult.Failure>(PetsCsvDeserializer.decode(input))
        val error = assertIs<ImportException.UnknownSpecies>(failure.error)
        assertEquals("dragon", error.speciesId)
    }

    @Test
    fun `accepts empty optional cells as null`() {
        val input = csv("Немо,fish,,2024-01-01,,,")

        val success = assertIs<PetsImportResult.Success>(PetsCsvDeserializer.decode(input))
        val entry = success.entries.single()
        assertEquals("Немо", entry.name)
        assertEquals("fish", entry.speciesId)
        assertNull(entry.subcategoryId)
        assertNull(entry.genderId)
        assertNull(entry.weightKg)
        assertNull(entry.notes)
    }

    @Test
    fun `handles BOM in UTF-8 encoded CSV from Excel`() {
        val withBom = "﻿" + csv("Рекс,dog,medium,2020-03-15,male,25.5,Любит мяч")

        val success = assertIs<PetsImportResult.Success>(PetsCsvDeserializer.decode(withBom))
        // BOM не должен прилипнуть к первой колонке заголовка и сломать сопоставление имён.
        assertEquals("Рекс", success.entries.single().name)
    }

    @Test
    fun `fails on empty input`() {
        val failure = assertIs<PetsImportResult.Failure>(PetsCsvDeserializer.decode(""))
        assertIs<ImportException.MalformedData>(failure.error)
    }

    @Test
    fun `fails on blank name cell`() {
        val input = csv("   ,dog,,2020-01-01,,,")

        val failure = assertIs<PetsImportResult.Failure>(PetsCsvDeserializer.decode(input))
        assertIs<ImportException.MalformedData>(failure.error)
    }

    @Test
    fun `fails on non-numeric weight_kg cell`() {
        val input = csv("Рекс,dog,,2020-01-01,,heavy,")

        val failure = assertIs<PetsImportResult.Failure>(PetsCsvDeserializer.decode(input))
        assertIs<ImportException.MalformedData>(failure.error)
    }

    @Test
    fun `unknown gender_id is a lenient warning not a failure`() {
        val input = csv("Рекс,dog,,2020-01-01,neuter,,")

        val success = assertIs<PetsImportResult.Success>(PetsCsvDeserializer.decode(input))
        assertEquals(1, success.entries.size)
        val warning = assertIs<ImportWarning.UnknownGender>(success.warnings.single())
        assertEquals("neuter", warning.genderId)
        assertEquals("Рекс", warning.petName)
    }

    @Test
    fun `ignores trailing blank line`() {
        val input = csv("Рекс,dog,,2020-01-01,,,") + "\r\n"

        val success = assertIs<PetsImportResult.Success>(PetsCsvDeserializer.decode(input))
        assertEquals(1, success.entries.size, "финальный перенос строки не создаёт пустую запись")
    }

    @Test
    fun `strips formula-injection guard apostrophe from name and notes (round-trip)`() {
        // Экспорт префиксует формульные значения апострофом; импорт должен снять его обратно.
        val malicious =
            Pet(
                id = 1L,
                name = "=cmd|'/c calc'!A1",
                species = Species.Dog,
                birthDate = LocalDate.of(2020, 1, 1),
                notes = "+1+1",
            )
        val input = PetsCsvSerializer.encode(listOf(malicious), Instant.parse("2026-05-29T12:00:00Z"))

        val success = assertIs<PetsImportResult.Success>(PetsCsvDeserializer.decode(input))
        val entry = success.entries.single()
        assertEquals("=cmd|'/c calc'!A1", entry.name, "ведущий апостроф-нейтрализатор снят при импорте")
        assertEquals("+1+1", entry.notes, "формульные заметки восстановлены без апострофа")
    }

    @Test
    fun `fails on unterminated quoted field`() {
        // Открывающая кавычка без закрывающей — обрезанный/битый CSV.
        val input = header + "\r\n" + "\"Рекс,dog,,2020-01-01,,,"

        val failure = assertIs<PetsImportResult.Failure>(PetsCsvDeserializer.decode(input))
        assertIs<ImportException.MalformedData>(failure.error)
    }
}
