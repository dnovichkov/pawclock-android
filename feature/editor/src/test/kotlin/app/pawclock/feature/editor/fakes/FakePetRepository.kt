package app.pawclock.feature.editor.fakes

import app.pawclock.domain.pet.PetRepository
import app.pawclock.domain.usecase.GetPetByIdUseCase
import app.pawclock.domain.usecase.SavePetUseCase
import app.pawclock.model.Pet
import java.time.Clock
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory fake [PetRepository] для юнит-тестов [PetEditorViewModel].
 *
 * Аналог [app.pawclock.feature.pets.fakes.FakePetRepository] (Task 18) — копируем здесь,
 * пока `:core:testing` не оформлен как shared-fixtures модуль (Plan 2).
 *
 * Поведение совпадает с production-реализацией [app.pawclock.data.pet.RoomPetRepository]:
 *  - сортировка `name.lowercase()` имитирует `ORDER BY name COLLATE NOCASE ASC`;
 *  - insert назначает auto-id ≥ 1L через [AtomicLong];
 *  - update бросает [NoSuchElementException], если запись не существует —
 *    защита от регрессии в SavePetUseCase (update должен быть осознанным выбором, не fallback'ом).
 */
class FakePetRepository : PetRepository {
    private val nextId = AtomicLong(1L)
    private val state = MutableStateFlow<List<Pet>>(emptyList())

    override fun observeAll(): Flow<List<Pet>> = state.asStateFlow()

    override suspend fun getById(id: Long): Pet? = state.value.firstOrNull { it.id == id }

    override suspend fun insert(pet: Pet): Long {
        val assignedId = if (pet.id == 0L) nextId.getAndIncrement() else pet.id
        val stored = pet.copy(id = assignedId)
        state.update { current -> (current + stored).sortedBy { it.name.lowercase() } }
        return assignedId
    }

    override suspend fun update(pet: Pet) {
        if (state.value.none { it.id == pet.id }) {
            throw NoSuchElementException("Cannot update pet with id=${pet.id}: not found in fake repository")
        }
        state.update { current ->
            current
                .map { if (it.id == pet.id) pet else it }
                .sortedBy { it.name.lowercase() }
        }
    }

    override suspend fun deleteById(id: Long): Int {
        val before = state.value.size
        state.update { current -> current.filterNot { it.id == id } }
        return before - state.value.size
    }

    fun seed(pets: List<Pet>) {
        state.value = pets.sortedBy { it.name.lowercase() }
    }

    fun savePetUseCase(clock: Clock = Clock.systemDefaultZone()): SavePetUseCase =
        SavePetUseCase(petRepository = this, clock = clock)

    fun getPetByIdUseCase(): GetPetByIdUseCase = GetPetByIdUseCase(this)
}
