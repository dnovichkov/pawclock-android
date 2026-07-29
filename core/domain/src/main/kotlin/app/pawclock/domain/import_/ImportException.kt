package app.pawclock.domain.import_

/**
 * Ошибки импорта данных питомцев (§3.5 спецификации, Plan 2 Task 19).
 *
 * Импорт читает недоверенный ввод (произвольный файл, выбранный пользователем через SAF),
 * поэтому каждая категория ошибки типизирована — UI (Task 21) маппит конкретный подтип на
 * локализованное сообщение. [PetsJsonDeserializer] возвращает эти исключения завёрнутыми в
 * [PetsImportResult.Failure] (не бросает), а [ImportPetsUseCase] пробрасывает их вызывающему.
 *
 * Пакет назван `import_` с подчёркиванием, т.к. `import` — зарезервированное слово Kotlin.
 */
sealed class ImportException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    /**
     * Ввод не является валидным документом ожидаемого формата: битый JSON/CSV, неверный тип
     * значения, некорректная дата рождения, пустое имя и т.п.
     */
    class MalformedData(
        message: String,
        cause: Throwable? = null,
    ) : ImportException(message, cause)

    /**
     * Отсутствует обязательное поле записи питомца (`name`, `species_id` или `birth_date`).
     *
     * @property fields имена недостающих полей (как они объявлены в схеме)
     */
    class MissingRequiredField(
        val fields: List<String>,
    ) : ImportException("Missing required field(s): ${fields.joinToString()}")

    /**
     * `species_id` записи не соответствует ни одному поддерживаемому виду — fail-fast,
     * без тихого пропуска записи (импорт неизвестного вида означал бы потерю данных).
     *
     * @property speciesId неизвестный идентификатор вида из ввода
     */
    class UnknownSpecies(
        val speciesId: String,
    ) : ImportException("Unknown species id: $speciesId")

    /**
     * `schema_version` файла больше поддерживаемой текущей версии (forward-incompatible):
     * формат мог измениться несовместимо, и наивный разбор исказил бы данные.
     *
     * @property version версия схемы из файла
     * @property supported максимальная поддерживаемая версия этим билдом приложения
     */
    class UnsupportedSchemaVersion(
        val version: Int,
        val supported: Int,
    ) : ImportException("Unsupported schema version $version (supported up to $supported)")
}
