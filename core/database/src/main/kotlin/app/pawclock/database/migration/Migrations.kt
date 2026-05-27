package app.pawclock.database.migration

import androidx.room.migration.Migration

/**
 * Реестр Room-миграций для [app.pawclock.database.db.PawClockDatabase].
 *
 * **В Plan 1 (version=1) миграций нет** — это baseline-схема.
 * При добавлении version=2 добавьте `MIGRATION_1_2: Migration(1, 2) { ... }` ниже
 * и включите его в [all], затем напишите тест в `androidTest/MigrationsTest.kt`
 * через `MigrationTestHelper` (см. Task 12 plan + §11.9 спецификации).
 *
 * Schema-диффы автогенерируются в `core/database/schemas/` (Room schema export).
 */
object Migrations {
    /**
     * Возвращает все зарегистрированные миграции для передачи в
     * `Room.databaseBuilder(...).addMigrations(*Migrations.all())`.
     */
    fun all(): Array<Migration> = emptyArray()
}
