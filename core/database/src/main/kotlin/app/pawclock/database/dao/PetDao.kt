package app.pawclock.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import app.pawclock.database.entity.PetEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для CRUD-операций над таблицей `pets`.
 *
 *  - [observeAll] — реактивный Flow, эмитирует новый список при каждом insert/update/delete
 *  - [getById] — одноразовый suspend для редактора питомца
 *  - [insert] — возвращает авто-сгенерированный id (Long), который UI использует для навигации
 *  - [update] — частичное обновление (Room сравнивает по PK)
 *  - [deleteById] / [delete] — два варианта удаления; deleteById удобнее для UI-слоя
 *
 * Сортировка `ORDER BY name COLLATE NOCASE ASC` гарантирует case-insensitive сортировку,
 * что важно для русской/английской смешанной локали.
 *
 * Все suspend-функции должны вызываться из coroutine с IO-диспетчером (Hilt-провайдер
 * обеспечивает это автоматически через Room.databaseBuilder).
 */
@Dao
interface PetDao {
    @Query("SELECT * FROM pets ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PetEntity?

    @Insert
    suspend fun insert(entity: PetEntity): Long

    @Update
    suspend fun update(entity: PetEntity)

    @Delete
    suspend fun delete(entity: PetEntity): Int

    @Query("DELETE FROM pets WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}
