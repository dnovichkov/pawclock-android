package app.pawclock.data.care.di

import android.content.Context
import app.pawclock.data.care.AndroidAssetSource
import app.pawclock.domain.care.AssetSource
import app.pawclock.domain.care.CareRepository
import app.pawclock.domain.care.CareRepositoryImpl

/**
 * Фабрика для CareRepository — placeholder Hilt-модуля (Task 14 / Plan 1).
 *
 * В Plan 1 `:app` ещё не имеет `@HiltAndroidApp` — поэтому модуль реализован как
 * простой factory object. В Task 17, когда будет создан `PawClockApplication`
 * с `@HiltAndroidApp`, этот файл будет переписан в `@Module @InstallIn(SingletonComponent::class)`
 * с `@Provides`/`@Binds`-методами:
 *
 * ```
 * @Module @InstallIn(SingletonComponent::class)
 * object CareModule {
 *   @Provides @Singleton
 *   fun provideAssetSource(@ApplicationContext ctx: Context): AssetSource = AndroidAssetSource(ctx)
 * }
 *
 * @Module @InstallIn(SingletonComponent::class)
 * abstract class CareRepositoryModule {
 *   @Binds @Singleton
 *   abstract fun bindCareRepository(impl: CareRepositoryImpl): CareRepository
 * }
 * ```
 *
 * До Task 17 — manually инстанцируемый wire-up через [createCareRepository].
 */
object CareModule {
    /**
     * Создаёт production-готовый [CareRepository], связанный с Android-assets через [context].
     *
     * Использование (до Task 17 / Hilt wiring):
     * ```
     * val careRepository = CareModule.createCareRepository(applicationContext)
     * ```
     */
    fun createCareRepository(context: Context): CareRepository {
        val assetSource: AssetSource = AndroidAssetSource(context.applicationContext)
        return CareRepositoryImpl(assetSource)
    }
}
