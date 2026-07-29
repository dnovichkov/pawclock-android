package app.pawclock.data.locale.di

import android.content.Context
import app.pawclock.domain.locale.LocaleApplier
import app.pawclock.locale.LocaleHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt-модуль, биндящий port [LocaleApplier] на Android-реализацию [LocaleHelper].
 *
 * Архитектурно: SettingsViewModel injectit'ит LocaleApplier (domain port), а
 * production-реализация — [LocaleHelper] в `:app/locale`, которому нужен
 * application context для framework LocaleManager (API 33+).
 * Unit-тесты SettingsViewModel'и подменяют через FakeLocaleApplier (см.
 * feature/settings/src/test/.../fakes/).
 */
@Module
@InstallIn(SingletonComponent::class)
object LocaleModule {
    @Provides
    @Singleton
    fun provideLocaleApplier(
        @ApplicationContext context: Context,
    ): LocaleApplier = LocaleHelper(context)
}
