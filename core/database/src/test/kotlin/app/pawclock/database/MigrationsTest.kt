package app.pawclock.database

import app.pawclock.database.migration.Migrations
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Sanity check for [Migrations] registry.
 *
 * В Plan 1 у нас version=1 — реальных миграций нет. Этот тест проверяет, что
 * скаффолд существует и возвращает корректный (пусть и пустой) массив.
 * При вводе MIGRATION_1_2 в Plan 2 этот тест расширится проверками SQL-стейтментов.
 */
class MigrationsTest {
    @Test
    fun `Migrations all returns empty array in v1 baseline`() {
        val migrations = Migrations.all()
        assertNotNull(migrations)
        // В v1 миграций нет — мы только заложили scaffold для будущих версий.
        assertTrue(
            migrations.isEmpty(),
            "Expected empty migrations array at v1; future versions add entries to Migrations.kt",
        )
    }
}
