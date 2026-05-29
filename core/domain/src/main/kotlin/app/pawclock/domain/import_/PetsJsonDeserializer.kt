package app.pawclock.domain.import_

import app.pawclock.domain.export.PetExportEntry
import app.pawclock.domain.export.PetsExportSchema
import app.pawclock.model.Gender
import app.pawclock.model.Species
import java.time.LocalDate
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Разбирает экспортный JSON обратно в записи питомцев (§3.5, Plan 2 Task 19).
 *
 * Обратная сторона [app.pawclock.domain.export.PetsJsonSerializer]: переиспользует ту же схему
 * [PetsExportSchema], но добавляет fail-fast валидацию недоверенного ввода. Pure-Kotlin, без
 * Android-зависимостей — живёт в `:core:domain` и тестируется на JVM.
 *
 * Контракт: метод [decode] НЕ бросает на ожидаемых ошибках данных — возвращает
 * [PetsImportResult.Failure] с типизированным [ImportException]. Бросают только программные баги.
 *
 * Порядок проверок (важен для предсказуемых сообщений):
 *  1. `schema_version` > текущей → [ImportException.UnsupportedSchemaVersion] (до полного декода,
 *     т.к. несовместимая схема могла бы исказить данные);
 *  2. декод в [PetsExportSchema] → [ImportException.MissingRequiredField] при отсутствии
 *     обязательного поля, иначе [ImportException.MalformedData] (битый JSON / неверный тип);
 *  3. по каждой записи: пустое `name` / неизвестный `species_id` / некорректная `birth_date` —
 *     fail-fast ([ImportException.MalformedData] / [ImportException.UnknownSpecies]);
 *  4. неизвестный `gender_id` → [ImportWarning.UnknownGender] (lenient, не отказ).
 */
object PetsJsonDeserializer {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * @param input содержимое импортируемого JSON-файла
     * @return [PetsImportResult.Success] с разобранными записями и предупреждениями,
     *   либо [PetsImportResult.Failure] с причиной отказа
     */
    fun decode(input: String): PetsImportResult {
        unsupportedVersion(input)?.let { return it }
        return parseAndValidate(input)
    }

    /**
     * Лёгкая предпроверка версии до полного декода: читает только `schema_version`. Если ввод не
     * разбирается как JSON-объект (битый / не объект / без поля), возвращает `null` — пусть полный
     * декод в [parseAndValidate] выдаст точную [ImportException.MalformedData].
     */
    private fun unsupportedVersion(input: String): PetsImportResult.Failure? {
        val version =
            runCatching {
                (Json.parseToJsonElement(input) as? JsonObject)
                    ?.get("schema_version")
                    ?.jsonPrimitive
                    ?.intOrNull
            }.getOrNull() ?: return null
        val supported = PetsExportSchema.CURRENT_SCHEMA_VERSION
        return if (version > supported) {
            fail(ImportException.UnsupportedSchemaVersion(version, supported))
        } else {
            null
        }
    }

    private fun parseAndValidate(input: String): PetsImportResult {
        val schema =
            runCatching { json.decodeFromString(PetsExportSchema.serializer(), input) }
                .getOrElse { return mapDecodeError(it) }
        return validateEntries(schema.pets)
    }

    private fun mapDecodeError(error: Throwable): PetsImportResult =
        when (error) {
            is MissingFieldException -> fail(ImportException.MissingRequiredField(error.missingFields))
            is SerializationException -> fail(ImportException.MalformedData("Malformed export schema", error))
            else -> throw error
        }

    private fun validateEntries(entries: List<PetExportEntry>): PetsImportResult {
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
        if (structuralError != null) return fail(structuralError)
        recordGenderWarning(entry, warnings)
        return null
    }

    private fun isParseableDate(value: String): Boolean = runCatching { LocalDate.parse(value) }.isSuccess

    private fun fail(error: ImportException): PetsImportResult.Failure = PetsImportResult.Failure(error)

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
