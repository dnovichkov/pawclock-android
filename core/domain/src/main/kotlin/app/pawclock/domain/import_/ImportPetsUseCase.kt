package app.pawclock.domain.import_

import app.pawclock.domain.export.ExportFormat
import app.pawclock.domain.export.PetExportEntry
import app.pawclock.domain.pet.PetRepository
import app.pawclock.model.Gender
import app.pawclock.model.Pet
import app.pawclock.model.Species
import java.time.Clock
import java.time.LocalDate

/**
 * Импортирует питомцев из строки-бэкапа в [PetRepository] (§3.5 спецификации, Plan 2 Task 19/20).
 *
 * Декодирует ввод соответствующим формату [PetsDeserializer], конвертирует [PetExportEntry] в
 * доменные [Pet] (с `id = 0L` — Room назначит новый PK; `photoPath = null` — фото не переносится)
 * и применяет выбранную [ImportStrategy]. На [PetsImportResult.Failure] пробрасывает
 * [ImportException] — UI-слой (Task 21) ловит его и показывает локализованную ошибку.
 *
 * Формат ([ExportFormat.JSON] / [ExportFormat.CSV]) определяется автоматически по содержимому
 * (JSON начинается с `{` или `[`, иначе CSV), либо задаётся явно параметром `format` — SAF может
 * вернуть файл любого из двух форматов.
 *
 * `dryRun = true` возвращает предпросмотр (количество и предупреждения) без мутации репозитория —
 * для confirm-диалога перед фактическим импортом.
 *
 * Доменная валидация ([validateImportable]) выполняется ДО любой мутации репозитория: это
 * (а) не даёт сохранить «битого» питомца (дата в будущем / нереалистичный год / некорректный вес),
 * которого [app.pawclock.domain.usecase.CalculatePetAgeUseCase] потом не сможет рассчитать, и
 * (б) гарантирует, что [ImportStrategy.REPLACE] не очистит существующие данные перед тем, как
 * наткнуться на невалидную запись (иначе — необратимая потеря данных).
 *
 * @param petRepository целевой репозиторий
 * @param clock источник «сегодня» для проверки даты рождения; в production — системные часы,
 *   в тестах — `Clock.fixed`
 */
class ImportPetsUseCase(
    private val petRepository: PetRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    /**
     * @param content содержимое импортируемого файла
     * @param strategy стратегия слияния с существующими данными
     * @param dryRun если `true` — только предпросмотр, без записи в репозиторий
     * @param format явный формат ввода; `null` — определить автоматически по содержимому
     * @return сводка импорта ([ImportSummary])
     * @throws ImportException если ввод не удалось разобрать
     */
    suspend operator fun invoke(
        content: String,
        strategy: ImportStrategy,
        dryRun: Boolean = false,
        format: ExportFormat? = null,
    ): ImportSummary {
        val deserializer =
            when (format ?: detectFormat(content)) {
                ExportFormat.JSON -> PetsJsonDeserializer
                ExportFormat.CSV -> PetsCsvDeserializer
            }
        val success =
            when (val result = deserializer.decode(content)) {
                is PetsImportResult.Failure -> throw result.error
                is PetsImportResult.Success -> result
            }
        val pets = success.entries.map(::toPet)
        validateImportable(pets)
        if (!dryRun) {
            when (strategy) {
                // Атомарная замена: clearAll + insert в одной транзакции (см. PetDao.replaceAll).
                // Иначе сбой вставки в середине цикла оставил бы пользователя без старых и новых данных.
                ImportStrategy.REPLACE -> petRepository.replaceAll(pets)
                ImportStrategy.MERGE -> pets.forEach { petRepository.insert(it) }
            }
        }
        return ImportSummary(
            importedCount = pets.size,
            warnings = success.warnings,
            dryRun = dryRun,
        )
    }

    /**
     * Доменная валидация уже разобранных питомцев — зеркалит правила
     * [app.pawclock.domain.usecase.SavePetUseCase] для обычного сохранения, т.к. импорт пишет в
     * репозиторий напрямую, минуя его. Fail-fast (как и остальной импорт): первая нарушенная запись
     * прерывает импорт с [ImportException.MalformedData], репозиторий при этом не тронут.
     */
    private fun validateImportable(pets: List<Pet>) {
        val today = LocalDate.now(clock)
        for (pet in pets) {
            val problem =
                when {
                    !pet.birthDate.isBefore(today) -> "birth_date must be in the past: ${pet.birthDate}"
                    pet.birthDate.year < EARLIEST_REALISTIC_BIRTH_YEAR ->
                        "birth_date year is unrealistic: ${pet.birthDate}"
                    pet.weightKg?.let { !it.isFinite() || it < 0.0 } == true ->
                        "weight_kg must be a non-negative finite number: ${pet.weightKg}"
                    else -> null
                }
            if (problem != null) {
                throw ImportException.MalformedData("$problem (\"${pet.name}\")")
            }
        }
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

    /**
     * Определяет формат по содержимому: JSON-документ начинается с `{` (объект схемы) или `[`,
     * иначе считаем CSV (заголовок `name,...`). Ведущий BOM и пробелы игнорируются.
     */
    private fun detectFormat(content: String): ExportFormat {
        val head = content.removePrefix("﻿").trimStart()
        return if (head.startsWith("{") || head.startsWith("[")) ExportFormat.JSON else ExportFormat.CSV
    }

    private companion object {
        /**
         * Граница «нереалистичной» даты рождения — синхронизирована с
         * `SavePetUseCase.EARLIEST_REALISTIC_BIRTH_YEAR`: дата раньше 1990 почти наверняка ошибка.
         */
        const val EARLIEST_REALISTIC_BIRTH_YEAR: Int = 1990
    }
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
