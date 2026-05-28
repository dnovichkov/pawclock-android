package app.pawclock.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import app.pawclock.datastore.SettingsRepository
import app.pawclock.datastore.SettingsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Имя файла preferences. Хранится в `applicationContext.filesDir/datastore/<name>.preferences_pb`.
 * Изменение имени = breaking change (потеряются сохранённые настройки).
 */
private const val SETTINGS_DATASTORE_NAME = "app_settings"

/**
 * Extension property для получения единственного `DataStore<Preferences>` на Context.
 *
 * `preferencesDataStore` — built-in delegate из `androidx.datastore.preferences.preferencesDataStore`,
 * который автоматически:
 *  - создаёт scope с `SupervisorJob() + Dispatchers.IO`;
 *  - подключает [ReplaceFileCorruptionHandler] (replace с emptyPreferences при corruption);
 *  - гарантирует один instance на pathname (singleton-by-path).
 *
 * Размещён на top-level в файле модуля, потому что `preferencesDataStore` создаёт
 * lazy-singleton по сайту вызова — повторное использование в разных местах было бы багом.
 */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = SETTINGS_DATASTORE_NAME,
)

/**
 * Hilt-модуль для DataStore Preferences и [SettingsRepository].
 *
 * Использует `SingletonComponent`, чтобы DataStore и репозиторий жили один раз
 * на жизненный цикл приложения. Тесты не используют этот модуль — они инстанцируют
 * [SettingsRepositoryImpl] напрямую с TestDataStore (см. `SettingsRepositoryTest`).
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun provideSettingsDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.settingsDataStore
}

/**
 * Биндинг интерфейса на реализацию для Hilt-DI.
 *
 * Разделено на отдельный @Module (не вложено в DataStoreModule object), потому что
 * `@Binds` обязан быть abstract — это правило Hilt/Dagger.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
