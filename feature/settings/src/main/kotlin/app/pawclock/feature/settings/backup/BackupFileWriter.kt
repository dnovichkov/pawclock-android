package app.pawclock.feature.settings.backup

/**
 * Порт записи бэкап-файла (§3.5 спецификации, Plan 2 Task 21).
 *
 * Изолирует [app.pawclock.feature.settings.SettingsViewModel] от Android `Uri` и
 * `ContentResolver`: ViewModel оперирует строковым представлением location'а (uri.toString()),
 * а конкретная реализация ([app.pawclock.data.saf.SafFileWriter] в `:app`) восстанавливает
 * `Uri` и пишет через `contentResolver.openOutputStream(uri)`. Это держит ViewModel
 * pure-Kotlin и JVM-тестируемым через fake-реализацию.
 */
fun interface BackupFileWriter {
    /**
     * Записывает [content] в location, заданный SAF-выбором пользователя.
     *
     * @param uriString строковое представление `content://`-Uri (`ACTION_CREATE_DOCUMENT`)
     * @param content сериализованное содержимое бэкапа (JSON или CSV)
     * @throws java.io.IOException если запись не удалась (нет доступа, отозванное разрешение и т.п.)
     */
    suspend fun write(
        uriString: String,
        content: String,
    )
}
