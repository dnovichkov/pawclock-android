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
 * Schema export ВРЕМЕННО ОТКЛЮЧЁН (Task 17, ⚠️ known issue):
 * Room 2.8.4 + kotlinx-serialization 1.8.1 + Kotlin 2.0.21 имеют binary-incompat между
 * `androidx.room.migration.bundle.FieldBundle$$serializer` (компилированный с серилизацией
 * 1.8.1+ defaults) и runtime-call'ами `typeParametersSerializers()`. Symptom:
 * `AbstractMethodError` при попытке Room прочитать existing schema JSON для diff.
 *
 * Schema-файл `core/database/schemas/.../1.json` сохранён в git (от Task 12) и будет
 * использоваться для миграционных тестов в Plan 2 после bump'а Kotlin → 2.1.x
 * (вместе с KSP 2.1.x), что устранит binary-incompat. До тех пор `exportSchema = false`
 * — KSP не пытается reads/writes JSON, build работает.
 *
 * @see app.pawclock.database.di.DatabaseModule Hilt-провайдер для production
 */
@Database(
    entities = [PetEntity::class],
    version = 1,
    exportSchema = false,
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
