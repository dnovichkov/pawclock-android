package app.pawclock.domain.pet

import app.pawclock.model.Pet
import kotlinx.coroutines.flow.Flow

/**
 * Доменный репозиторий-port для CRUD-операций над питомцами.
 *
 * Объявлен в `:core:domain`, чтобы UseCase'ы зависели только от чистого Kotlin-интерфейса,
 * без знания о Room/SQLite/DataStore-деталях. Конкретная реализация
 * (`RoomPetRepository`) живёт в `:core:database` или `:app/data` и подключается через Hilt.
 *
 * Контракт реализации:
 *  - [observeAll] — реактивный Flow, эмитирует новый список при каждом insert/update/delete.
 *    Сортировка определяется реализацией (обычно `ORDER BY name COLLATE NOCASE ASC`).
 *  - [getById] — одноразовый suspend, возвращает `null` если питомец не найден.
 *  - [insert] — возвращает авто-сгенерированный id (Long), который UI использует для навигации.
 *  - [update] — частичное обновление (реализация сравнивает по PK).
 *  - [deleteById] — возвращает количество удалённых строк (0 для отсутствующего id).
 *
 * Все suspend-методы должны вызываться из coroutine — реализация ответственна за
 * переключение на IO-диспетчер.
 */
interface PetRepository {
    fun observeAll(): Flow<List<Pet>>

    suspend fun getById(id: Long): Pet?

    suspend fun insert(pet: Pet): Long

    suspend fun update(pet: Pet)

    suspend fun deleteById(id: Long): Int
}
