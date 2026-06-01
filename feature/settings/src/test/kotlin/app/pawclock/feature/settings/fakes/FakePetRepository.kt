package app.pawclock.feature.settings.fakes

import app.pawclock.domain.pet.PetRepository
import app.pawclock.model.Pet
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory fake [PetRepository] для JVM-тестов [app.pawclock.feature.settings.SettingsViewModel].
 *
 * Минимальный аналог доменного fake (`:core:domain` test): достаточно для проверки export/import
 * флоу через реальные [app.pawclock.domain.export.ExportPetsUseCase] /
 * [app.pawclock.domain.import_.ImportPetsUseCase] поверх него.
 */
internal class FakePetRepository(
    initial: List<Pet> = emptyList(),
) : PetRepository {
    private val nextId = AtomicLong(1L)
    private val state = MutableStateFlow(initial)

    override fun observeAll(): Flow<List<Pet>> = state.asStateFlow()

    override suspend fun getAll(): List<Pet> = state.value

    override suspend fun getById(id: Long): Pet? = state.value.firstOrNull { it.id == id }

    override suspend fun insert(pet: Pet): Long {
        val assignedId = if (pet.id == 0L) nextId.getAndIncrement() else pet.id
        state.update { current -> current + pet.copy(id = assignedId) }
        return assignedId
    }

    override suspend fun update(pet: Pet) {
        state.update { current -> current.map { if (it.id == pet.id) pet else it } }
    }

    override suspend fun deleteById(id: Long): Int {
        val before = state.value.size
        state.update { current -> current.filterNot { it.id == id } }
        return before - state.value.size
    }

    override suspend fun clearAll() {
        state.value = emptyList()
    }
}
