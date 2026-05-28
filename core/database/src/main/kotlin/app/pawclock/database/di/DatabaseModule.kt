package app.pawclock.database.di

import android.content.Context
import androidx.room.Room
import app.pawclock.database.dao.PetDao
import app.pawclock.database.db.PawClockDatabase
import app.pawclock.database.migration.Migrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt-модуль для предоставления Room-database и DAO в граф зависимостей.
 *
 * Использует `SingletonComponent`, поэтому БД создаётся один раз на жизненный
 * цикл приложения. Production-builder читает из файла на диске
 * `Context.databases/[PawClockDatabase.DATABASE_NAME]`.
 *
 * Тесты используют `Room.inMemoryDatabaseBuilder` напрямую (без Hilt) — см.
 * `androidTest/PetDaoTest.kt`.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun providePawClockDatabase(
        @ApplicationContext context: Context,
    ): PawClockDatabase {
        val builder =
            Room.databaseBuilder(
                context,
                PawClockDatabase::class.java,
                PawClockDatabase.DATABASE_NAME,
            )
        // Регистрируем миграции по одной — избегаем spread-операторного копирования массива
        // и красиво ведём себя при пустом Migrations.all() (Plan 1, version=1).
        Migrations.all().forEach { builder.addMigrations(it) }
        return builder.build()
    }

    @Provides
    fun providePetDao(database: PawClockDatabase): PetDao = database.petDao()
}
