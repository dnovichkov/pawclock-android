package app.pawclock.domain.usecase

import app.pawclock.domain.pet.PetRepository

/**
 * Удаляет питомца по id.
 *
 * Возвращает `true`, если строка была удалена, `false` — если питомец с таким id
 * не существовал (либо был удалён ранее в другой coroutine). UI использует это,
 * чтобы решить, показывать ли Snackbar «удалено» или silent fail.
 */
class DeletePetUseCase(
    private val petRepository: PetRepository,
) {
    suspend operator fun invoke(id: Long): Boolean = petRepository.deleteById(id) > 0
}
