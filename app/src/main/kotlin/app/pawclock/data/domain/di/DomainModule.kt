package app.pawclock.data.domain.di

import app.pawclock.calculator.CatAgeCalculator
import app.pawclock.calculator.CatLifeStageCalculator
import app.pawclock.calculator.DogAgeCalculator
import app.pawclock.calculator.DogLifeStageCalculator
import app.pawclock.data.settings.DataStoreSettingsReader
import app.pawclock.datastore.SettingsRepository
import app.pawclock.domain.care.CareRepository
import app.pawclock.domain.pet.PetRepository
import app.pawclock.domain.settings.SettingsReader
import app.pawclock.domain.usecase.CalculatePetAgeUseCase
import app.pawclock.domain.usecase.DeletePetUseCase
import app.pawclock.domain.usecase.GetCareRecommendationsUseCase
import app.pawclock.domain.usecase.GetPetByIdUseCase
import app.pawclock.domain.usecase.GetPetsUseCase
import app.pawclock.domain.usecase.SavePetUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt-модуль для доменного слоя — UseCase'ы и калькуляторы (Task 17 / Plan 1).
 *
 * Используется `@Provides` (а не `@Binds` с `@Inject constructor`), чтобы оставить
 * `:core:domain` и `:core:calculator` чистыми pure-Kotlin модулями без зависимости
 * на `javax.inject`. UseCase'ы и калькуляторы — обычные классы; Hilt их инстанцирует
 * через factory-методы ниже.
 *
 * SettingsReader — adapter поверх `SettingsRepository` из `:core:datastore`,
 * изолирующий domain от знания о DataStore (port-and-adapter pattern).
 *
 * Калькуляторы предоставляются как `@Singleton` (stateless, дёшево держать), UseCase'ы —
 * без скоупа (новый instance на @Inject point, минимальный footprint).
 *
 * Зависимость на [PetRepository] разрешается в [app.pawclock.data.pet.di.PetModule]
 * (создаётся в Task 18 вместе с Room-backed реализацией). До тех пор Hilt-граф
 * валиден: ни один `@HiltViewModel` пока не запрашивает UseCase'ы, требующие
 * PetRepository, поэтому отсутствие binding'а не приводит к compile-time ошибке.
 */
@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    @Provides
    @Singleton
    fun provideDogAgeCalculator(): DogAgeCalculator = DogAgeCalculator()

    @Provides
    @Singleton
    fun provideDogLifeStageCalculator(): DogLifeStageCalculator = DogLifeStageCalculator()

    @Provides
    @Singleton
    fun provideCatAgeCalculator(): CatAgeCalculator = CatAgeCalculator()

    @Provides
    @Singleton
    fun provideCatLifeStageCalculator(): CatLifeStageCalculator = CatLifeStageCalculator()

    @Provides
    @Singleton
    fun provideSettingsReader(settingsRepository: SettingsRepository): SettingsReader =
        DataStoreSettingsReader(settingsRepository)

    @Provides
    fun provideCalculatePetAgeUseCase(
        dogAgeCalculator: DogAgeCalculator,
        dogLifeStageCalculator: DogLifeStageCalculator,
        catAgeCalculator: CatAgeCalculator,
        catLifeStageCalculator: CatLifeStageCalculator,
        settingsReader: SettingsReader,
    ): CalculatePetAgeUseCase =
        CalculatePetAgeUseCase(
            dogAgeCalculator = dogAgeCalculator,
            dogLifeStageCalculator = dogLifeStageCalculator,
            catAgeCalculator = catAgeCalculator,
            catLifeStageCalculator = catLifeStageCalculator,
            settingsReader = settingsReader,
        )

    @Provides
    fun provideGetPetsUseCase(petRepository: PetRepository): GetPetsUseCase = GetPetsUseCase(petRepository)

    @Provides
    fun provideGetPetByIdUseCase(petRepository: PetRepository): GetPetByIdUseCase = GetPetByIdUseCase(petRepository)

    @Provides
    fun provideSavePetUseCase(petRepository: PetRepository): SavePetUseCase = SavePetUseCase(petRepository)

    @Provides
    fun provideDeletePetUseCase(petRepository: PetRepository): DeletePetUseCase = DeletePetUseCase(petRepository)

    @Provides
    fun provideGetCareRecommendationsUseCase(careRepository: CareRepository): GetCareRecommendationsUseCase =
        GetCareRecommendationsUseCase(careRepository)
}
