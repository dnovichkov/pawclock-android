package app.pawclock.data.saf

import android.content.Context
import android.net.Uri
import app.pawclock.feature.settings.backup.BackupFileReader
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Storage Access Framework реализация [BackupFileReader] (§3.5, Plan 2 Task 21).
 *
 * Читает содержимое файла, выбранного через `ACTION_OPEN_DOCUMENT`. Симметрична
 * [SafFileWriter]: принимает uri как строку и парсит в [Uri] на границе Android-слоя.
 *
 * `openInputStream` может вернуть `null` — трактуем как [IOException]. Файл читается целиком
 * в строку UTF-8 (бэкапы малы — десятки питомцев) на [Dispatchers.IO].
 */
class SafFileReader
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : BackupFileReader {
        override suspend fun read(uriString: String): String =
            withContext(Dispatchers.IO) {
                val uri = Uri.parse(uriString)
                val stream =
                    context.contentResolver.openInputStream(uri)
                        ?: throw IOException("Cannot open input stream for $uriString")
                stream.use { it.readBytes().toString(Charsets.UTF_8) }
            }
    }
