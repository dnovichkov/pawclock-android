package app.pawclock.data.saf.di

import app.pawclock.data.saf.SafFileReader
import app.pawclock.data.saf.SafFileWriter
import app.pawclock.feature.settings.backup.BackupFileReader
import app.pawclock.feature.settings.backup.BackupFileWriter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt-модуль для SAF I/O бэкапов (§3.5, Plan 2 Task 21).
 *
 * Замыкает порты `:feature:settings` ([BackupFileWriter] / [BackupFileReader]) на
 * Android-реализации из `:app/data/saf`. Используется `@Binds`: реализации — обычные
 * `@Inject`-классы с `@ApplicationContext Context`, фабрика не нужна.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SafModule {
    @Binds
    @Singleton
    abstract fun bindBackupFileWriter(impl: SafFileWriter): BackupFileWriter

    @Binds
    @Singleton
    abstract fun bindBackupFileReader(impl: SafFileReader): BackupFileReader
}
