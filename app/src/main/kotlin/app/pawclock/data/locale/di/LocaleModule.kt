package app.pawclock.data.locale.di

import app.pawclock.domain.locale.LocaleApplier
import app.pawclock.locale.LocaleHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt-модуль, биндящий port [LocaleApplier] на Android-реализацию [LocaleHelper].
 *
 * Архитектурно: SettingsViewModel injectit'ит LocaleApplier (domain port), а
 * production-реализация — singleton object LocaleHelper в `:app/locale`.
 * Unit-тесты SettingsViewModel'и подменяют через FakeLocaleApplier (см.
 * feature/settings/src/test/.../fakes/).
 *
 * Используется `@Provides` (а не `@Binds`) — LocaleHelper это Kotlin object, и через
 * @Binds Hilt бы хотел абстрактный класс/interface как первый параметр.
 */
@Module
@InstallIn(SingletonComponent::class)
object LocaleModule {
    @Provides
    @Singleton
    fun provideLocaleApplier(): LocaleApplier = LocaleHelper
}
