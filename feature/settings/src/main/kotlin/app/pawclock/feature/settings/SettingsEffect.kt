package app.pawclock.feature.settings

import app.pawclock.domain.import_.ImportWarning

/**
 * One-time эффекты экрана настроек (§3.5 спецификации, Plan 2 Task 21, MVI).
 *
 * В отличие от [SettingsState] (реактивный, реплеится новым подписчикам), эффект — разовое
 * событие, которое нельзя «реплеить» при пересоздании Activity: запуск SAF-диалога, показ
 * Snackbar. Доставляется через `Channel` → `Flow`, который UI коллектит ровно один раз.
 *
 * Экспорт/импорт двухфазны, потому что выбор файла идёт через системный SAF-диалог
 * (`ACTION_CREATE_DOCUMENT` / `ACTION_OPEN_DOCUMENT`), результат которого приходит асинхронно:
 *  1. пользователь инициирует действие → ViewModel эмитит [RequestSaveLocation] / [RequestOpenLocation];
 *  2. UI запускает Activity Result launcher → пользователь выбирает `Uri`;
 *  3. UI шлёт [SettingsEvent.ExportLocationSelected] / [SettingsEvent.ImportLocationSelected] →
 *     ViewModel выполняет операцию и эмитит [ExportComplete] / [ImportComplete] / ошибку.
 */
sealed interface SettingsEffect {
    /**
     * Запросить у пользователя расположение для сохранения бэкапа (`ACTION_CREATE_DOCUMENT`).
     *
     * @property suggestedFilename имя файла по умолчанию (с расширением `.json` / `.csv`)
     * @property mimeType MIME-тип создаваемого документа (`application/json` / `text/csv`)
     */
    data class RequestSaveLocation(
        val suggestedFilename: String,
        val mimeType: String,
    ) : SettingsEffect

    /**
     * Запросить у пользователя файл для импорта (`ACTION_OPEN_DOCUMENT`).
     *
     * @property mimeTypes допустимые MIME-типы (фильтр системного пикера)
     */
    data class RequestOpenLocation(
        val mimeTypes: List<String>,
    ) : SettingsEffect

    /**
     * Экспорт успешно записан в выбранный файл.
     *
     * @property petCount число экспортированных питомцев (для сообщения пользователю)
     */
    data class ExportComplete(
        val petCount: Int,
    ) : SettingsEffect

    /**
     * Экспорт не удался (ошибка записи в файл).
     *
     * @property messageKey стабильный ключ локализованного сообщения (см. [SettingsMessages])
     */
    data class ExportError(
        val messageKey: String,
    ) : SettingsEffect

    /**
     * Импорт успешно применён к репозиторию.
     *
     * @property petCount число импортированных питомцев
     * @property warnings нефатальные предупреждения, накопленные при разборе
     */
    data class ImportComplete(
        val petCount: Int,
        val warnings: List<ImportWarning>,
    ) : SettingsEffect

    /**
     * Импорт не удался (битый файл, неизвестный вид, ошибка чтения и т.п.).
     *
     * @property messageKey стабильный ключ локализованного сообщения (см. [SettingsMessages])
     */
    data class ImportError(
        val messageKey: String,
    ) : SettingsEffect
}

/**
 * Стабильные ключи сообщений результата экспорта/импорта.
 *
 * ViewModel остаётся свободной от Android `R`, эмитя ключ; UI ([app.pawclock.feature.settings.ui])
 * маппит ключ на `stringResource`. Это сохраняет JVM-тестируемость ViewModel и единый источник
 * правды для ассертов в тестах.
 */
object SettingsMessages {
    const val IMPORT_ERROR_MALFORMED: String = "import_error_malformed"
    const val IMPORT_ERROR_MISSING_FIELD: String = "import_error_missing_field"
    const val IMPORT_ERROR_UNKNOWN_SPECIES: String = "import_error_unknown_species"
    const val IMPORT_ERROR_UNSUPPORTED_VERSION: String = "import_error_unsupported_version"
    const val IMPORT_ERROR_READ_FAILED: String = "import_error_read_failed"
    const val EXPORT_ERROR_WRITE_FAILED: String = "export_error_write_failed"
}
