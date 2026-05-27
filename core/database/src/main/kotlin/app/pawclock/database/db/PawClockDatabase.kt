package app.pawclock.database.db

import androidx.room.Database
import androidx.room.RoomDatabase
import app.pawclock.database.dao.PetDao
import app.pawclock.database.entity.PetEntity

/**
 * Корневой Room-database для PawClock.
 *
 * **Версия 1 (Plan 1):** одна таблица `pets`.
 * Миграции на будущие версии регистрируются в `app.pawclock.database.migration.Migrations`.
 *
 * Schema export включён (`exportSchema = true`) — JSON-схема сохраняется в
 * `core/database/schemas/app.pawclock.database.db.PawClockDatabase/1.json`.
 * Эти файлы должны коммититься, чтобы автотесты миграций могли диффить версии.
 *
 * @see app.pawclock.database.di.DatabaseModule Hilt-провайдер для production
 */
@Database(
    entities = [PetEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class PawClockDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao

    companion object {
        /** Имя файла БД в `Context.databases/`. */
        const val DATABASE_NAME = "pawclock.db"

        /** Текущая версия схемы — синхронизируйте с [Database.version] выше. */
        const val DATABASE_VERSION = 1
    }
}
