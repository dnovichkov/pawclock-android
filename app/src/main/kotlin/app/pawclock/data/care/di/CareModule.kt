package app.pawclock.data.care.di

import android.content.Context
import app.pawclock.data.care.AndroidAssetSource
import app.pawclock.domain.care.AssetSource
import app.pawclock.domain.care.CareRepository
import app.pawclock.domain.care.CareRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt-модуль для care-recommendations подсистемы (Task 17 / Plan 1).
 *
 * Поставляет:
 *  - [AssetSource] → [AndroidAssetSource] поверх `Context.assets` (production)
 *  - [CareRepository] → [CareRepositoryImpl] поверх [AssetSource]
 *
 * Используется `@Provides` (а не `@Binds`), чтобы оставить `:core:domain` чистым
 * pure-Kotlin модулем без зависимости на `javax.inject`. [CareRepositoryImpl] и
 * [AndroidAssetSource] — обычные классы с обычными конструкторами; Hilt их вызывает
 * через factory-методы ниже.
 *
 * Обе зависимости — `@Singleton`, потому что они stateless и кэширование инстансов
 * предсказуемо снижает аллокации на горячем пути care-recommendations загрузки.
 */
@Module
@InstallIn(SingletonComponent::class)
object CareModule {
    @Provides
    @Singleton
    fun provideAssetSource(
        @ApplicationContext context: Context,
    ): AssetSource = AndroidAssetSource(context)

    @Provides
    @Singleton
    fun provideCareRepository(assetSource: AssetSource): CareRepository = CareRepositoryImpl(assetSource)
}
