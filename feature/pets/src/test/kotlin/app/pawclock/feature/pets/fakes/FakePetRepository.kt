package app.pawclock.feature.pets.fakes

import app.pawclock.domain.pet.PetRepository
import app.pawclock.domain.usecase.GetPetByIdUseCase
import app.pawclock.domain.usecase.GetPetsUseCase
import app.pawclock.model.Pet
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory fake [PetRepository] для юнит-тестов feature-ViewModel'ей.
 *
 * Копия [app.pawclock.domain.fakes.FakePetRepository] из `:core:domain/src/test/`,
 * перенесённая сюда из-за невозможности использовать `testFixtures()`-зависимости
 * в текущей версии Kotlin/AGP (1.10 stretch goal, в Plan 1 пока inline-fakes).
 *
 * Поведение совпадает с production [PetRepository] реализацией:
 *  - реактивный Flow через [MutableStateFlow] — Turbine-тесты могут проверять
 *    последовательные эмиссии после CRUD-операций;
 *  - сортировка `compareBy({ it.name.lowercase() })` имитирует поведение
 *    Room `ORDER BY name COLLATE NOCASE ASC`.
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

    fun getPetsUseCase(): GetPetsUseCase = GetPetsUseCase(this)

    fun getPetByIdUseCase(): GetPetByIdUseCase = GetPetByIdUseCase(this)
}

/**
 * Особая реализация [PetRepository] — Flow никогда не эмитит элементов.
 *
 * Используется для проверки initial-state ViewModel'и (PetsListState.Loading,
 * PetDetailState.Loading), когда нужно убедиться, что состояние Loading реально
 * возникает до первой эмиссии репозитория.
 */
class PausedFakePetRepository : PetRepository {
    override fun observeAll(): Flow<List<Pet>> = kotlinx.coroutines.flow.emptyFlow()

    override suspend fun getById(id: Long): Pet? = null

    override suspend fun insert(pet: Pet): Long = error("not implemented for paused fake")

    override suspend fun update(pet: Pet) = error("not implemented for paused fake")

    override suspend fun deleteById(id: Long): Int = 0

    fun getPetsUseCase(): GetPetsUseCase = GetPetsUseCase(this)

    fun getPetByIdUseCase(): GetPetByIdUseCase = GetPetByIdUseCase(this)
}
