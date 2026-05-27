package app.pawclock.model

/**
 * Режим темы оформления приложения.
 *
 * Используется в `AppSettings` (см. `:core:datastore`) и применяется композаблом
 * `PawClockTheme` (см. `:core:designsystem`, Task 16).
 *
 * Хранится в DataStore Preferences по стабильному [id] (а НЕ по `name`),
 * чтобы переименование констант в Kotlin не ломало уже сохранённые preference'ы.
 *
 * См. спецификацию PawClock §5 "Дизайн" и §6 "Настройки".
 */
enum class ThemeMode(
    val id: String,
) {
    /** Принудительно светлая тема. */
    Light(id = "light"),

    /** Принудительно тёмная тема. */
    Dark(id = "dark"),

    /** Следовать системной настройке (default). */
    System(id = "system"),
    ;

    companion object {
        /**
         * Возвращает [ThemeMode] по стабильному [id], либо `null` если значение
         * не распознано (повреждённый DataStore, миграция со старой версии).
         */
        fun fromId(id: String): ThemeMode? = entries.firstOrNull { it.id == id }
    }
}
