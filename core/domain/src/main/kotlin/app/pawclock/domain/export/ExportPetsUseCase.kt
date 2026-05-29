package app.pawclock.domain.export

import app.pawclock.domain.pet.PetRepository
import java.time.Clock

/**
 * Экспортирует всех питомцев в строку выбранного формата (§3.5 спецификации, Plan 2 Task 17).
 *
 * Читает полный снимок через [PetRepository.getAll] (а не реактивный `observeAll`, т.к. экспорт —
 * одноразовая операция) и делегирует сериализацию формат-специфичному сериализатору. Возвращает
 * строку, а не пишет в файл: запись через SAF (`ACTION_CREATE_DOCUMENT`) — ответственность UI-слоя
 * (Task 21), что держит UseCase Android-независимым и легко тестируемым.
 *
 * Момент экспорта берётся из инжектированного [Clock] (в тестах — `Clock.fixed`), поэтому
 * `exported_at` детерминирован.
 *
 * @param petRepository источник данных питомцев
 * @param clock источник времени для `exported_at`; в production — `Clock.systemDefaultZone()`
 */
class ExportPetsUseCase(
    private val petRepository: PetRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    /**
     * @param format формат экспорта ([ExportFormat.JSON] / [ExportFormat.CSV])
     * @return сериализованное содержимое бэкапа
     */
    suspend operator fun invoke(format: ExportFormat): String {
        val pets = petRepository.getAll()
        val exportedAt = clock.instant()
        return when (format) {
            ExportFormat.JSON -> PetsJsonSerializer.encode(pets, exportedAt)
            ExportFormat.CSV -> PetsCsvSerializer.encode(pets, exportedAt)
        }
    }
}
