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

/**
 * Тесты для [PetsCsvSerializer] (Plan 2, Task 18).
 *
 * Проверяют контракт экспортного CSV по RFC 4180: фиксированный snake_case-заголовок,
 * экранирование запятых/кавычек/переносов строк, пустые ячейки для `null` (а не литерал
 * `"null"`) и ISO-8601 даты. Разделитель записей — CRLF (`\r\n`), как требует RFC 4180.
 */
class PetsCsvSerializerTest {
    private val exportedAt: Instant = Instant.parse("2026-05-29T12:00:00Z")
    private val header = "name,species_id,subcategory_id,birth_date,gender_id,weight_kg,notes"

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

    private fun lines(csv: String): List<String> = csv.split("\r\n")

    @Test
    fun `serializes single Dog pet to CSV with header row`() {
        val csv = PetsCsvSerializer.encode(listOf(rex), exportedAt)

        val rows = lines(csv)
        assertEquals(2, rows.size, "должна быть строка-заголовок + одна строка данных")
        assertEquals(header, rows[0])
        assertEquals("Рекс,dog,medium,2020-03-15,male,25.5,Любит мяч", rows[1])
    }

    @Test
    fun `header row uses snake_case column names`() {
        val csv = PetsCsvSerializer.encode(emptyList(), exportedAt)

        assertEquals(header, lines(csv).first())
    }

    @Test
    fun `escapes commas and quotes in name and notes fields`() {
        val tricky =
            rex.copy(
                name = "Рекс, \"Большой\"",
                notes = "ест, спит",
            )
        val csv = PetsCsvSerializer.encode(listOf(tricky), exportedAt)

        // запятая → поле в кавычках; внутренняя кавычка удваивается ("" по RFC 4180)
        assertTrue(
            csv.contains("\"Рекс, \"\"Большой\"\"\""),
            "имя с запятой и кавычками должно быть закавычено и кавычки удвоены",
        )
        assertTrue(csv.contains("\"ест, спит\""), "заметки с запятой должны быть закавычены")
    }

    @Test
    fun `newlines in notes are escaped as quoted fields`() {
        val multiline = rex.copy(notes = "первая\nвторая")
        val csv = PetsCsvSerializer.encode(listOf(multiline), exportedAt)

        // перенос строки внутри поля → поле закавычено и НЕ разрывает запись
        assertTrue(csv.contains("\"первая\nвторая\""), "перенос строки в заметках должен быть закавычен")
        assertEquals(2, lines(csv).size, "встроенный \\n не должен разрывать запись на две")
    }

    @Test
    fun `null fields are empty cells not the string null`() {
        val minimal =
            Pet(
                id = 5L,
                name = "Немо",
                species = Species.Fish,
                birthDate = LocalDate.of(2024, 1, 1),
            )
        val csv = PetsCsvSerializer.encode(listOf(minimal), exportedAt)

        val rows = lines(csv)
        assertEquals("Немо,fish,,2024-01-01,,,", rows[1])
        assertFalse(csv.contains("null"), "null-поля должны быть пустыми ячейками, не литералом null")
    }

    @Test
    fun `birthDate is ISO-8601 string`() {
        val csv = PetsCsvSerializer.encode(listOf(rex), exportedAt)

        val birthDateCell = lines(csv)[1].split(",")[3]
        assertEquals(LocalDate.of(2020, 3, 15), LocalDate.parse(birthDateCell))
    }
}
