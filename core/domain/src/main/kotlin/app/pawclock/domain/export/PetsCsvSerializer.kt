package app.pawclock.domain.export

import app.pawclock.model.Pet
import java.time.Instant

/**
 * Сериализует список [Pet] в CSV по RFC 4180 (§3.5, Plan 2 Task 18).
 *
 * Табличный формат для совместимости с Excel/Google Sheets — человекочитаемый, но плоский
 * (в отличие от [PetsJsonSerializer], который сохраняет версионируемую схему). Колонки повторяют
 * поля [PetExportEntry] минус `id`/`photoPath`: последние — локальные artifact'ы и не экспортируются.
 *
 * Экранирование по [RFC 4180](https://www.rfc-editor.org/rfc/rfc4180):
 *  - поле, содержащее запятую, двойную кавычку или перенос строки, заключается в двойные кавычки;
 *  - внутренняя двойная кавычка удваивается (`"` → `""`);
 *  - разделитель записей — CRLF (`\r\n`); финального перевода строки нет (последняя запись по RFC
 *    может не иметь завершающего line break — это упрощает round-trip с парсером Task 20).
 *
 * `null`-поля сериализуются как пустые ячейки (а не литерал `"null"`), что позволяет импорту
 * (Task 20) восстанавливать их обратно в `null`.
 *
 * Защита от CSV formula injection (OWASP): свободные пользовательские поля (`name`, `notes`)
 * могут начинаться с `=`/`+`/`-`/`@`/TAB/CR — Excel/Sheets/LibreOffice трактуют такую ячейку как
 * формулу и выполняют её при открытии файла. Перед записью такие значения префиксуются апострофом
 * (`'`) — стандартная нейтрализация; импорт ([PetsCsvDeserializer]) снимает префикс симметрично,
 * сохраняя round-trip. Числовые/enum/дата-поля не экранируются: они не являются свободным вводом.
 */
object PetsCsvSerializer {
    /** Строка-заголовок: стабильные snake_case-имена колонок, совпадающие с `@SerialName` JSON-схемы. */
    private const val HEADER = "name,species_id,subcategory_id,birth_date,gender_id,weight_kg,notes"

    /** Разделитель записей по RFC 4180. */
    private const val RECORD_SEPARATOR = "\r\n"

    /**
     * Ведущие символы, делающие ячейку формулой в Excel/Google Sheets/LibreOffice.
     * `internal`, т.к. [PetsCsvDeserializer] переиспользует тот же набор для симметричного снятия.
     */
    internal val FORMULA_TRIGGERS: Set<Char> = setOf('=', '+', '-', '@', '\t', '\r')

    /** Префикс-нейтрализатор формулы (OWASP CSV injection). */
    internal const val FORMULA_GUARD: Char = '\''

    /**
     * Нейтрализует CSV formula injection: если значение начинается с [FORMULA_TRIGGERS], префиксует
     * его [FORMULA_GUARD]. Применяется только к свободным текстовым полям перед RFC-экранированием.
     */
    internal fun guardAgainstFormulaInjection(value: String): String =
        if (value.isNotEmpty() && value.first() in FORMULA_TRIGGERS) "$FORMULA_GUARD$value" else value

    /**
     * @param pets питомцы для экспорта (в порядке, заданном вызывающим — обычно сортировка репозитория)
     * @param exportedAt принят для симметрии сигнатуры с [PetsJsonSerializer.encode] (единообразный
     *   диспатч в [ExportPetsUseCase]); в плоский CSV момент экспорта не пишется — преамбула сломала бы
     *   контракт «первая строка = заголовок»
     * @return CSV-строка с заголовком и одной записью на питомца
     */
    @Suppress("UNUSED_PARAMETER")
    fun encode(
        pets: List<Pet>,
        exportedAt: Instant,
    ): String =
        buildString {
            append(HEADER)
            for (pet in pets) {
                append(RECORD_SEPARATOR)
                append(row(pet))
            }
        }

    private fun row(pet: Pet): String =
        listOf(
            guardAgainstFormulaInjection(pet.name),
            pet.species.id,
            pet.subcategory,
            pet.birthDate.toString(),
            pet.gender?.id,
            pet.weightKg?.toString(),
            pet.notes?.let(::guardAgainstFormulaInjection),
        ).joinToString(separator = ",") { escape(it) }

    /**
     * Экранирует одну ячейку по RFC 4180. `null` → пустая ячейка; значения с `,`/`"`/`\r`/`\n`
     * заключаются в кавычки с удвоением внутренних кавычек.
     */
    private fun escape(value: String?): String {
        if (value == null) return ""
        val needsQuoting = value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        return if (needsQuoting) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
