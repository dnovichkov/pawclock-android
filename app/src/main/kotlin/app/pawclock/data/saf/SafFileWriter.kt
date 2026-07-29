package app.pawclock.data.saf

import android.content.Context
import android.net.Uri
import app.pawclock.feature.settings.backup.BackupFileWriter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Storage Access Framework реализация [BackupFileWriter] (§3.5, Plan 2 Task 21).
 *
 * Пишет содержимое бэкапа в `content://`-Uri, выбранный пользователем через
 * `ACTION_CREATE_DOCUMENT`. Принимает uri как строку (порт [BackupFileWriter] изолирует
 * ViewModel от Android `Uri`) и парсит её обратно в [Uri] здесь, на границе Android-слоя.
 *
 * `openOutputStream` может вернуть `null` (provider не отдал stream) — трактуем как [IOException],
 * чтобы ViewModel показал единое сообщение об ошибке записи. I/O выполняется на [Dispatchers.IO].
 *
 * Режим `"wt"` (write + truncate): по умолчанию `"w"` у многих DocumentsProvider НЕ усекает файл,
 * поэтому при перезаписи существующего файла большего размера в конце оставались бы «хвостовые»
 * байты старого содержимого — получился бы битый JSON/CSV. `"wt"` гарантирует усечение.
 */
class SafFileWriter
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : BackupFileWriter {
        override suspend fun write(
            uriString: String,
            content: String,
        ) = withContext(Dispatchers.IO) {
            val uri = Uri.parse(uriString)
            val stream =
                context.contentResolver.openOutputStream(uri, "wt")
                    ?: throw IOException("Cannot open output stream for $uriString")
            stream.use { it.write(content.toByteArray(Charsets.UTF_8)) }
        }
    }
