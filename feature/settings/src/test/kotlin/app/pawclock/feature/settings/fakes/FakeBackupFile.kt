package app.pawclock.feature.settings.fakes

import app.pawclock.feature.settings.backup.BackupFileReader
import app.pawclock.feature.settings.backup.BackupFileWriter
import java.io.IOException

/**
 * In-memory fake [BackupFileWriter] для JVM-тестов: захватывает записанное содержимое и uri
 * вместо реального SAF I/O. Может имитировать ошибку записи через [failWith].
 */
internal class FakeBackupFileWriter(
    private val failWith: IOException? = null,
) : BackupFileWriter {
    var lastUri: String? = null
        private set
    var lastContent: String? = null
        private set
    var writeCount: Int = 0
        private set

    override suspend fun write(
        uriString: String,
        content: String,
    ) {
        failWith?.let { throw it }
        lastUri = uriString
        lastContent = content
        writeCount++
    }
}

/**
 * In-memory fake [BackupFileReader] для JVM-тестов: возвращает заранее заданное [content]
 * вместо реального SAF I/O. Может имитировать ошибку чтения через [failWith].
 */
internal class FakeBackupFileReader(
    private val content: String = "",
    private val failWith: IOException? = null,
) : BackupFileReader {
    var lastUri: String? = null
        private set

    override suspend fun read(uriString: String): String {
        failWith?.let { throw it }
        lastUri = uriString
        return content
    }
}
