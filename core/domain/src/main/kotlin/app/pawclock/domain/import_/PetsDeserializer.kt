package app.pawclock.domain.import_

/**
 * Разбирает строку-бэкап питомцев в [PetsImportResult] (§3.5, Plan 2 Task 20).
 *
 * Единый контракт для форматных реализаций ([PetsJsonDeserializer], [PetsCsvDeserializer]),
 * позволяющий [ImportPetsUseCase] выбирать декодер по формату ввода и подменять его в тестах.
 *
 * Контракт: реализации НЕ бросают на ожидаемых ошибках данных — возвращают
 * [PetsImportResult.Failure] с типизированным [ImportException]. Бросают только программные баги.
 */
fun interface PetsDeserializer {
    /**
     * @param input содержимое импортируемого файла (JSON или CSV в зависимости от реализации)
     * @return [PetsImportResult.Success] с записями и предупреждениями, либо [PetsImportResult.Failure]
     */
    fun decode(input: String): PetsImportResult
}
