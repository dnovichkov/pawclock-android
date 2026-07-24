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
 *  - [getAll] — одноразовый suspend-снимок всех питомцев (для экспорта, §3.5). В отличие от
 *    [observeAll], не подписывается на изменения — экспорт читает текущее состояние один раз.
 *  - [getById] — одноразовый suspend, возвращает `null` если питомец не найден.
 *  - [insert] — возвращает авто-сгенерированный id (Long), который UI использует для навигации.
 *  - [update] — частичное обновление (реализация сравнивает по PK).
 *  - [deleteById] — возвращает количество удалённых строк (0 для отсутствующего id).
 *  - [clearAll] — удаляет всех питомцев (для импорта со стратегией REPLACE, §3.5).
 *  - [replaceAll] — атомарно заменяет всех питомцев (clearAll + insert в одной транзакции),
 *    для импорта со стратегией REPLACE (§3.5).
 *
 * Все suspend-методы должны вызываться из coroutine — реализация ответственна за
 * переключение на IO-диспетчер.
 */
interface PetRepository {
    fun observeAll(): Flow<List<Pet>>

    suspend fun getAll(): List<Pet>

    suspend fun getById(id: Long): Pet?

    suspend fun insert(pet: Pet): Long

    suspend fun update(pet: Pet)

    suspend fun deleteById(id: Long): Int

    suspend fun clearAll()

    /**
     * Заменяет всё содержимое репозитория списком [pets] как единую неделимую операцию.
     *
     * Дефолтная реализация (`clearAll` + поэлементный `insert`) корректна по результату, но НЕ
     * атомарна — её достаточно для in-memory тестовых фейков. Persistence-реализация (Room) обязана
     * переопределить метод транзакцией, чтобы сбой на середине вставки не оставил репозиторий
     * частично очищенным (потеря данных при импорте REPLACE).
     */
    suspend fun replaceAll(pets: List<Pet>) {
        clearAll()
        pets.forEach { insert(it) }
    }
}
