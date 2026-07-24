package app.pawclock.domain.import_

import app.pawclock.domain.export.PetExportEntry
import app.pawclock.domain.export.PetsCsvSerializer

/**
 * Разбирает экспортный CSV обратно в записи питомцев (§3.5, Plan 2 Task 20).
 *
 * Обратная сторона [app.pawclock.domain.export.PetsCsvSerializer]: pure-Kotlin парсер по
 * [RFC 4180](https://www.rfc-editor.org/rfc/rfc4180). После табличного разбора делегирует
 * достоверность записей общему [ImportEntryValidator] — той же политике, что и [PetsJsonDeserializer].
 *
 * Особенности разбора:
 *  - конечный автомат с состоянием `inQuotes` корректно обрабатывает поля с запятыми, переносами
 *    строк и удвоенными кавычками (`""` → `"`) внутри закавыченных ячеек;
 *  - разделитель записей — CRLF или одиночный LF (терпим к файлам из разных инструментов);
 *  - ведущий BOM (`U+FEFF`, который Excel дописывает в UTF-8 CSV) снимается, иначе он прилип бы
 *    к первой колонке заголовка и сломал сопоставление имён;
 *  - пустые ячейки необязательных колонок → `null` (симметрично экспорту);
 *  - финальный перенос строки не создаёт фиктивную пустую запись.
 *
 * Контракт [decode] — как у [PetsJsonDeserializer]: на ожидаемых ошибках данных возвращается
 * [PetsImportResult.Failure], а не бросается исключение.
 */
object PetsCsvDeserializer : PetsDeserializer {
    private const val BOM = '﻿'
    private const val COLUMN_NAME = "name"
    private const val COLUMN_SPECIES = "species_id"
    private const val COLUMN_BIRTH_DATE = "birth_date"
    private const val COLUMN_SUBCATEGORY = "subcategory_id"
    private const val COLUMN_GENDER = "gender_id"
    private const val COLUMN_WEIGHT = "weight_kg"
    private const val COLUMN_NOTES = "notes"

    private val REQUIRED_COLUMNS = listOf(COLUMN_NAME, COLUMN_SPECIES, COLUMN_BIRTH_DATE)

    /**
     * @param input содержимое импортируемого CSV-файла (с заголовком в первой строке)
     * @return [PetsImportResult.Success] с записями и предупреждениями, либо [PetsImportResult.Failure]
     */
    override fun decode(input: String): PetsImportResult {
        // parseRows возвращает null при незакрытой кавычке; .orEmpty() сводит оба «нет заголовка»
        // случая (битый ввод / пустой файл) к одной ветке elvis ниже — без лишнего return (detekt).
        val parsed = parseRows(input.removePrefix(BOM.toString()))
        val rows = parsed?.filterNot(::isBlankLine).orEmpty()
        val header =
            rows.firstOrNull()
                ?: return PetsImportResult.Failure(
                    ImportException.MalformedData(
                        if (parsed == null) "Unterminated quoted field in CSV" else "Empty CSV input",
                    ),
                )
        val columns = header.withIndex().associate { (index, name) -> name to index }
        val missing = REQUIRED_COLUMNS.filterNot(columns::containsKey)
        return when {
            missing.isNotEmpty() -> PetsImportResult.Failure(ImportException.MissingRequiredField(missing))
            else -> mapEntries(rows.drop(1), columns)
        }
    }

    private fun mapEntries(
        dataRows: List<List<String>>,
        columns: Map<String, Int>,
    ): PetsImportResult {
        val entries = ArrayList<PetExportEntry>(dataRows.size)
        for (row in dataRows) {
            val weightCell = optional(row, columns, COLUMN_WEIGHT)
            val weight = weightCell?.toDoubleOrNull()
            if (weightCell != null && weight == null) {
                return PetsImportResult.Failure(ImportException.MalformedData("Invalid weight_kg: $weightCell"))
            }
            entries +=
                PetExportEntry(
                    name = stripFormulaGuard(required(row, columns, COLUMN_NAME)),
                    speciesId = required(row, columns, COLUMN_SPECIES),
                    birthDate = required(row, columns, COLUMN_BIRTH_DATE),
                    subcategoryId = optional(row, columns, COLUMN_SUBCATEGORY),
                    genderId = optional(row, columns, COLUMN_GENDER),
                    weightKg = weight,
                    notes = optional(row, columns, COLUMN_NOTES)?.let(::stripFormulaGuard),
                )
        }
        return ImportEntryValidator.validate(entries)
    }

    /** Обязательная ячейка: гарантированно присутствующая колонка; отсутствие значения → пустая строка
     *  (валидатор затем отвергнет пустое имя / пустой вид / пустую дату). */
    private fun required(
        row: List<String>,
        columns: Map<String, Int>,
        name: String,
    ): String = cell(row, columns, name).orEmpty()

    /** Необязательная ячейка: отсутствующая колонка или пустое значение → `null`. */
    private fun optional(
        row: List<String>,
        columns: Map<String, Int>,
        name: String,
    ): String? = cell(row, columns, name)?.ifEmpty { null }

    private fun cell(
        row: List<String>,
        columns: Map<String, Int>,
        name: String,
    ): String? = columns[name]?.let(row::getOrNull)

    /**
     * Снимает анти-injection префикс ([PetsCsvSerializer.FORMULA_GUARD]), добавленный экспортом к
     * свободным полям, начинавшимся с формульного символа — обеспечивает round-trip `=Rex` ↔ `'=Rex`.
     * Применяется только когда за апострофом действительно следует формульный символ, чтобы не
     * затронуть имена, которые легитимно начинаются с апострофа без формулы.
     */
    private fun stripFormulaGuard(value: String): String =
        if (value.length >= 2 &&
            value.first() == PetsCsvSerializer.FORMULA_GUARD &&
            value[1] in PetsCsvSerializer.FORMULA_TRIGGERS
        ) {
            value.substring(1)
        } else {
            value
        }

    /** Пустая строка файла (одна пустая ячейка) — артефакт финального переноса строки, пропускается. */
    private fun isBlankLine(row: List<String>): Boolean = row.size == 1 && row.first().isEmpty()

    /**
     * Разбирает CSV в строки ячеек по RFC 4180 (конечный автомат).
     *
     * Состояние [inQuotes] определяет, является ли `,` / `\r` / `\n` разделителем или обычным
     * символом внутри закавыченного поля. Удвоенная кавычка (`""`) внутри кавычек экранирует одну.
     *
     * @return разобранные строки, либо `null` если ввод закончился с незакрытой кавычкой
     *   (битый/обрезанный CSV) — вызывающий трактует это как [ImportException.MalformedData].
     */
    @Suppress("CyclomaticComplexMethod", "NestedBlockDepth")
    private fun parseRows(input: String): List<List<String>>? {
        val rows = mutableListOf<List<String>>()
        var row = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var index = 0
        while (index < input.length) {
            val char = input[index]
            when {
                inQuotes ->
                    if (char == '"') {
                        if (input.getOrNull(index + 1) == '"') {
                            field.append('"')
                            index++
                        } else {
                            inQuotes = false
                        }
                    } else {
                        field.append(char)
                    }
                char == '"' -> inQuotes = true
                char == ',' -> {
                    row.add(field.toString())
                    field.clear()
                }
                char == '\r' || char == '\n' -> {
                    row.add(field.toString())
                    field.clear()
                    rows.add(row)
                    row = mutableListOf()
                    if (char == '\r' && input.getOrNull(index + 1) == '\n') index++
                }
                else -> field.append(char)
            }
            index++
        }
        if (inQuotes) return null
        row.add(field.toString())
        rows.add(row)
        return rows
    }
}
