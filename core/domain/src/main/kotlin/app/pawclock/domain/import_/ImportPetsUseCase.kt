package app.pawclock.domain.import_

import app.pawclock.domain.export.PetExportEntry
import app.pawclock.domain.pet.PetRepository
import app.pawclock.model.Gender
import app.pawclock.model.Pet
import app.pawclock.model.Species
import java.time.LocalDate

/**
 * Импортирует питомцев из строки-бэкапа в [PetRepository] (§3.5 спецификации, Plan 2 Task 19).
 *
 * Декодирует ввод через [PetsJsonDeserializer], конвертирует [PetExportEntry] в доменные [Pet]
 * (с `id = 0L` — Room назначит новый PK; `photoPath = null` — фото не переносится) и применяет
 * выбранную [ImportStrategy]. На [PetsImportResult.Failure] пробрасывает [ImportException] —
 * UI-слой (Task 21) ловит его и показывает локализованную ошибку.
 *
 * `dryRun = true` возвращает предпросмотр (количество и предупреждения) без мутации репозитория —
 * для confirm-диалога перед фактическим импортом.
 *
 * @param petRepository целевой репозиторий
 * @param deserializer декодер ввода (по умолчанию [PetsJsonDeserializer]; параметризован для тестов
 *   и для будущей диспетчеризации форматов в Task 20)
 */
class ImportPetsUseCase(
    private val petRepository: PetRepository,
    private val deserializer: PetsJsonDeserializer = PetsJsonDeserializer,
) {
    /**
     * @param content содержимое импортируемого файла
     * @param strategy стратегия слияния с существующими данными
     * @param dryRun если `true` — только предпросмотр, без записи в репозиторий
     * @return сводка импорта ([ImportSummary])
     * @throws ImportException если ввод не удалось разобрать
     */
    suspend operator fun invoke(
        content: String,
        strategy: ImportStrategy,
        dryRun: Boolean = false,
    ): ImportSummary {
        val success =
            when (val result = deserializer.decode(content)) {
                is PetsImportResult.Failure -> throw result.error
                is PetsImportResult.Success -> result
            }
        val pets = success.entries.map(::toPet)
        if (!dryRun) {
            if (strategy == ImportStrategy.REPLACE) {
                petRepository.clearAll()
            }
            pets.forEach { petRepository.insert(it) }
        }
        return ImportSummary(
            importedCount = pets.size,
            warnings = success.warnings,
            dryRun = dryRun,
        )
    }

    /**
     * Конвертирует уже валидированную [PetExportEntry] в доменный [Pet]. Не-null утверждения
     * безопасны: [PetsJsonDeserializer] заранее проверил вид, дату и непустое имя. Нераспознанный
     * пол был залогирован как предупреждение и здесь деградирует к `null`.
     */
    private fun toPet(entry: PetExportEntry): Pet =
        Pet(
            id = 0L,
            name = entry.name,
            species = requireNotNull(Species.fromString(entry.speciesId)),
            birthDate = LocalDate.parse(entry.birthDate),
            subcategory = entry.subcategoryId,
            gender = entry.genderId?.let(Gender::fromId),
            weightKg = entry.weightKg,
            notes = entry.notes,
            photoPath = null,
        )
}

/**
 * Итог операции импорта.
 *
 * @property importedCount число импортированных питомцев (при `dryRun` — сколько было бы импортировано)
 * @property warnings нефатальные замечания, накопленные при разборе
 * @property dryRun был ли это предпросмотр (без записи)
 */
data class ImportSummary(
    val importedCount: Int,
    val warnings: List<ImportWarning>,
    val dryRun: Boolean,
)
