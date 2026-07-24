package app.pawclock.domain.import_

import app.pawclock.domain.export.PetExportEntry
import app.pawclock.model.Gender
import app.pawclock.model.Species
import java.time.LocalDate

/**
 * Общая пост-валидация разобранных записей питомцев для JSON и CSV импорта (§3.5, Plan 2 Task 20).
 *
 * И [PetsJsonDeserializer], и [PetsCsvDeserializer] после структурного разбора (декод схемы /
 * парсинг RFC 4180) приводят ввод к списку [PetExportEntry], а затем применяют одни и те же
 * правила достоверности — поэтому логика вынесена сюда, чтобы не дублироваться между форматами.
 *
 * Политика (одинакова для обоих форматов):
 *  - пустое `name` → fail-fast [ImportException.MalformedData];
 *  - неизвестный `species_id` → fail-fast [ImportException.UnknownSpecies] (импорт неизвестного
 *    вида означал бы потерю данных);
 *  - некорректная `birth_date` (не ISO-8601) → fail-fast [ImportException.MalformedData];
 *  - неизвестный `gender_id` → lenient [ImportWarning.UnknownGender] (пол отбрасывается к `null`,
 *    запись принимается — косметическое поле не оправдывает отказ).
 */
internal object ImportEntryValidator {
    /**
     * @param entries сырые записи, полученные из формат-специфичного разбора
     * @return [PetsImportResult.Success] (с накопленными предупреждениями) либо первая
     *   [PetsImportResult.Failure] (fail-fast)
     */
    fun validate(entries: List<PetExportEntry>): PetsImportResult {
        val warnings = mutableListOf<ImportWarning>()
        for (entry in entries) {
            val failure = validateEntry(entry, warnings)
            if (failure != null) return failure
        }
        return PetsImportResult.Success(entries, warnings)
    }

    /**
     * Проверяет одну запись и накапливает предупреждения. Возвращает [PetsImportResult.Failure]
     * при фатальной ошибке (fail-fast) или `null`, если запись принята.
     */
    private fun validateEntry(
        entry: PetExportEntry,
        warnings: MutableList<ImportWarning>,
    ): PetsImportResult.Failure? {
        val structuralError =
            when {
                entry.name.isBlank() -> ImportException.MalformedData("Pet name must not be blank")
                Species.fromString(entry.speciesId) == null -> ImportException.UnknownSpecies(entry.speciesId)
                !isParseableDate(entry.birthDate) -> ImportException.MalformedData("Invalid birth_date format")
                else -> null
            }
        if (structuralError != null) return PetsImportResult.Failure(structuralError)
        recordGenderWarning(entry, warnings)
        return null
    }

    private fun isParseableDate(value: String): Boolean = runCatching { LocalDate.parse(value) }.isSuccess

    private fun recordGenderWarning(
        entry: PetExportEntry,
        warnings: MutableList<ImportWarning>,
    ) {
        val genderId = entry.genderId ?: return
        if (Gender.fromId(genderId) == null) {
            warnings += ImportWarning.UnknownGender(entry.name, genderId)
        }
    }
}
