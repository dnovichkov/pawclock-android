package app.pawclock.domain.import_

import app.pawclock.domain.export.PetExportEntry

/**
 * Результат разбора импортируемого файла десериализатором (§3.5, Plan 2 Task 19).
 *
 * Result-тип, а не исключения: [PetsJsonDeserializer.decode] никогда не бросает на ожидаемых
 * ошибках данных — это разделяет «разбор» и «политику реакции на ошибку». [ImportPetsUseCase]
 * сам решает пробросить [Failure.error] (для UI-обработки) или показать [Success.warnings].
 */
sealed interface PetsImportResult {
    /**
     * Файл успешно разобран.
     *
     * @property entries записи питомцев (сырые [PetExportEntry], общие со схемой экспорта);
     *   доменными [app.pawclock.model.Pet] они становятся в [ImportPetsUseCase]
     * @property warnings нефатальные замечания (например, нераспознанный пол)
     */
    data class Success(
        val entries: List<PetExportEntry>,
        val warnings: List<ImportWarning>,
    ) : PetsImportResult

    /**
     * Файл отклонён.
     *
     * @property error типизированная причина отказа
     */
    data class Failure(
        val error: ImportException,
    ) : PetsImportResult
}
