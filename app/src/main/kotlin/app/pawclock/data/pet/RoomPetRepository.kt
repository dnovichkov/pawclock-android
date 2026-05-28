package app.pawclock.data.pet

import app.pawclock.database.dao.PetDao
import app.pawclock.database.mapper.PetMapper
import app.pawclock.domain.pet.PetRepository
import app.pawclock.model.Pet
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed реализация [PetRepository] (Task 18 / Plan 1).
 *
 * Тонкий адаптер: делегирует все операции в [PetDao], конвертируя domain [Pet] ↔
 * persistence [app.pawclock.database.entity.PetEntity] через [PetMapper].
 *
 * Сортировка `ORDER BY name COLLATE NOCASE ASC` обеспечивается на стороне SQL
 * (см. [PetDao.observeAll]), поэтому маппер не выполняет дополнительной сортировки.
 *
 * Threading: Room автоматически переключает suspend-методы на IO-диспетчер; Flow
 * из `observeAll` коллектится в любом dispatcher'е, на котором запущен `collect { }`
 * (обычно `viewModelScope.launch { }` с Dispatchers.Main.immediate).
 *
 * Все исключения PetMapper'а (data corruption — unknown species/gender id) пробрасываются
 * выше; UI-слой должен их ловить и показывать Error-state.
 */
@Singleton
class RoomPetRepository
    @Inject
    constructor(
        private val petDao: PetDao,
    ) : PetRepository {
        override fun observeAll(): Flow<List<Pet>> =
            petDao.observeAll().map { entities -> entities.map(PetMapper::toDomain) }

        override suspend fun getById(id: Long): Pet? = petDao.getById(id)?.let(PetMapper::toDomain)

        override suspend fun insert(pet: Pet): Long = petDao.insert(PetMapper.toEntity(pet))

        override suspend fun update(pet: Pet) = petDao.update(PetMapper.toEntity(pet))

        override suspend fun deleteById(id: Long): Int = petDao.deleteById(id)
    }
