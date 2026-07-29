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
 * **Plan 2 (10 новых видов) — миграция НЕ требуется, версия остаётся 1.**
 * Подкатегории всех новых видов (RabbitSize, HamsterType, BirdType, ReptileType,
 * HorseType, FishType) хранятся в существующей колонке `subcategory TEXT` как стабильные
 * id-строки. Поскольку колонка уже принимает любую строку, расширение domain-enum'ов
 * не меняет SQL-схему — следовательно `DATABASE_VERSION` остаётся 1, `Migrations.all()`
 * пуст, а `PetMapper` не требует правок (он передаёт subcategory насквозь, не зная вида).
 * Это прямое следствие schema-decoupling, заложенного в [app.pawclock.database.entity.PetEntity]:
 * «изменять domain-enum'ы без миграции БД, если id остаются стабильными».
 * Контракт round-trip новых id зафиксирован в `PetMapperTest` (Plan 2 Task 12).
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
