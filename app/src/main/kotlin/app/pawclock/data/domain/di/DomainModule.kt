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

/**
 * Placeholder фабрика UseCase-слоя (Task 15 / Plan 1).
 *
 * В Plan 1 `:app` ещё не имеет `@HiltAndroidApp` (см. Task 17 в плане) и не подключает
 * Hilt-runtime. Поэтому DI собран как простая объект-фабрика по аналогии с
 * [app.pawclock.data.care.di.CareModule].
 *
 * В Task 17 этот файл будет переписан в `@Module @InstallIn(SingletonComponent::class)`
 * следующим образом:
 *
 * ```
 * @Module @InstallIn(SingletonComponent::class)
 * object DomainModule {
 *   @Provides fun provideSettingsReader(settingsRepository: SettingsRepository): SettingsReader =
 *     DataStoreSettingsReader(settingsRepository)
 *
 *   @Provides fun provideCalculatePetAgeUseCase(...): CalculatePetAgeUseCase =
 *     CalculatePetAgeUseCase(...)
 *
 *   // и т.д. для каждого UseCase
 * }
 * ```
 *
 * Все UseCase'ы — простые классы без аннотаций (`@Inject constructor` не используется),
 * поэтому Hilt-миграция в Task 17 — чисто механическое добавление `@Provides`-методов
 * без изменений production-кода.
 *
 * До Task 17 — UseCase'ы создаются вручную через [createUseCases] (либо отдельные create-методы
 * для интеграционных тестов, которым нужен подмножество UseCase'ов).
 */
object DomainModule {
    /**
     * Создаёт production-готовый адаптер [SettingsReader] поверх DataStore-репозитория.
     *
     * Используется как для production wire-up, так и для интеграционных тестов
     * `:app:testDebugUnitTest`, которым нужен полный реактивный поток.
     */
    fun createSettingsReader(settingsRepository: SettingsRepository): SettingsReader =
        DataStoreSettingsReader(settingsRepository)

    /**
     * Создаёт полный набор UseCase'ов для production-DI.
     *
     * @param petRepository production-реализация репозитория питомцев (будет создана
     *   в более поздней задаче, использующей Room — см. Task 18 в плане).
     * @param careRepository production-реализация care-репозитория (см. Task 14 + [app.pawclock.data.care.di.CareModule]).
     * @param settingsRepository репозиторий настроек (создаётся в DataStoreModule).
     */
    fun createUseCases(
        petRepository: PetRepository,
        careRepository: CareRepository,
        settingsRepository: SettingsRepository,
    ): UseCases {
        val settingsReader = createSettingsReader(settingsRepository)
        return UseCases(
            calculatePetAge =
                CalculatePetAgeUseCase(
                    dogAgeCalculator = DogAgeCalculator(),
                    dogLifeStageCalculator = DogLifeStageCalculator(),
                    catAgeCalculator = CatAgeCalculator(),
                    catLifeStageCalculator = CatLifeStageCalculator(),
                    settingsReader = settingsReader,
                ),
            getPets = GetPetsUseCase(petRepository),
            getPetById = GetPetByIdUseCase(petRepository),
            savePet = SavePetUseCase(petRepository),
            deletePet = DeletePetUseCase(petRepository),
            getCareRecommendations = GetCareRecommendationsUseCase(careRepository),
        )
    }

    /**
     * Пакет UseCase'ов, возвращаемый из [createUseCases] для удобного wire-up.
     *
     * До Task 17 (Hilt) ViewModel'и в feature-модулях создаются вручную; этот контейнер
     * избавляет от длинных параметр-списков в фабриках. После Task 17 — заменяется на
     * прямую инъекцию `@Inject` в ViewModel-конструкторы.
     */
    data class UseCases(
        val calculatePetAge: CalculatePetAgeUseCase,
        val getPets: GetPetsUseCase,
        val getPetById: GetPetByIdUseCase,
        val savePet: SavePetUseCase,
        val deletePet: DeletePetUseCase,
        val getCareRecommendations: GetCareRecommendationsUseCase,
    )
}
