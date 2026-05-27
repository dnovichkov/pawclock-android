package app.pawclock.domain.fakes

import app.pawclock.domain.pet.PetRepository
import app.pawclock.model.Pet
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory fake [PetRepository] для юнит-тестов UseCase-слоя.
 *
 * Особенности:
 *  - реактивный Flow через [MutableStateFlow] — Turbine-тесты могут проверять
 *    последовательные эмиссии после CRUD-операций;
 *  - сортировка `compareBy({ it.name.lowercase() })` имитирует поведение
 *    Room `ORDER BY name COLLATE NOCASE ASC` для предсказуемости тестов;
 *  - auto-increment id через [AtomicLong] — потокобезопасно (хотя в JVM-тестах
 *    обычно single-threaded);
 *  - insert с `pet.id == 0L` назначает новый id; insert с `pet.id != 0L` использует переданный id
 *    (полезно для setup-методов в тестах, чтобы вручную задать id);
 *  - update возвращает Unit (как у production-интерфейса), бросает `NoSuchElementException`
 *    при отсутствии питомца — это помогает обнаруживать баги в тестах раньше.
 *
 * Не Thread-safe для одновременного read+write из множества потоков; этого достаточно
 * для юнит-тестов с `runTest`-блоками.
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

    /** Тестовый helper для прямой подмены содержимого без прохождения CRUD-проверок. */
    fun seed(pets: List<Pet>) {
        state.value = pets.sortedBy { it.name.lowercase() }
    }
}
