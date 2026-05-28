package app.pawclock.domain.usecase

import app.pawclock.domain.pet.PetRepository
import app.pawclock.model.Pet
import kotlinx.coroutines.flow.Flow

/**
 * Возвращает реактивный поток списка питомцев.
 *
 * Сортировка определяется реализацией [PetRepository] (по умолчанию case-insensitive
 * для ASCII через SQLite `COLLATE NOCASE` — для Cyrillic применяется бинарная сортировка
 * по кодпоинту, см. KDoc `PetDao.observeAll`). UseCase сортировку не делает сам —
 * это ответственность persistence-слоя ради эффективности.
 */
class GetPetsUseCase(
    private val petRepository: PetRepository,
) {
    operator fun invoke(): Flow<List<Pet>> = petRepository.observeAll()
}
