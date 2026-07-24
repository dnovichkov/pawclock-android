package app.pawclock.feature.settings.backup

/**
 * Порт чтения бэкап-файла (§3.5 спецификации, Plan 2 Task 21).
 *
 * Симметричен [BackupFileWriter]: изолирует [app.pawclock.feature.settings.SettingsViewModel]
 * от Android `Uri`/`ContentResolver`. Реализация ([app.pawclock.data.saf.SafFileReader] в `:app`)
 * читает через `contentResolver.openInputStream(uri)`. Формат содержимого (JSON/CSV) определяется
 * ниже по стеку — в [app.pawclock.domain.import_.ImportPetsUseCase] (auto-detect).
 */
fun interface BackupFileReader {
    /**
     * Читает содержимое файла, выбранного пользователем через SAF.
     *
     * @param uriString строковое представление `content://`-Uri (`ACTION_OPEN_DOCUMENT`)
     * @return полное текстовое содержимое файла
     * @throws java.io.IOException если чтение не удалось (файл недоступен, отозванное разрешение и т.п.)
     */
    suspend fun read(uriString: String): String
}
