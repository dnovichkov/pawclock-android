package app.pawclock.domain.usecase

import app.pawclock.domain.pet.PetRepository
import app.pawclock.model.Pet

/**
 * Возвращает одного питомца по id или `null`, если не найден.
 *
 * Используется PetDetailScreen и PetEditorScreen (режим редактирования).
 * Возврат `null` (а не throw) — потому что отсутствие питомца это нормальный
 * runtime-кейс (например, питомец был удалён, пока экран был в backstack).
 */
class GetPetByIdUseCase(
    private val petRepository: PetRepository,
) {
    suspend operator fun invoke(id: Long): Pet? = petRepository.getById(id)
}
