package app.pawclock.feature.pets.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import app.pawclock.calculator.CatAgeCalculator
import app.pawclock.calculator.CatLifeStageCalculator
import app.pawclock.calculator.DogAgeCalculator
import app.pawclock.calculator.DogLifeStageCalculator
import app.pawclock.domain.usecase.CalculatePetAgeUseCase
import app.pawclock.domain.usecase.GetCareRecommendationsUseCase
import app.pawclock.feature.pets.common.LocaleProvider
import app.pawclock.feature.pets.fakes.FakeCareRepository
import app.pawclock.feature.pets.fakes.FakePetRepository
import app.pawclock.feature.pets.fakes.FakeSettingsReader
import app.pawclock.model.CareRecommendation
import app.pawclock.model.LifeStage
import app.pawclock.model.Pet
import app.pawclock.model.Species
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

/**
 * TDD-тесты для [PetDetailViewModel] (Task 18 / Plan 1).
 *
 * Поведение:
 *  1. Стартовое состояние — [PetDetailState.Loading].
 *  2. Если pet с заданным id не найден — переход в [PetDetailState.NotFound].
 *  3. Если найден — параллельно загружается calculated age + care recommendation,
 *     результат публикуется как [PetDetailState.Success].
 *  4. petId извлекается из SavedStateHandle (Navigation Compose 2.8 typesafe route).
 *
 * Тесты используют [StandardTestDispatcher] для контроля порядка выполнения coroutine'ов:
 *  - Loading наблюдается перед первым advanceUntilIdle()
 *  - Success/NotFound — после.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PetDetailViewModelTest {
    private val testClock: Clock =
        Clock.fixed(
            LocalDate.of(2026, 5, 27).atStartOfDay(ZoneOffset.UTC).toInstant(),
            ZoneOffset.UTC,
        )

    private fun samplePet(
        id: Long = 1L,
        species: Species = Species.Dog,
        subcategory: String? = "medium",
        birthDate: LocalDate = LocalDate.of(2021, 5, 27),
    ): Pet =
        Pet(
            id = id,
            name = "Rex",
            species = species,
            birthDate = birthDate,
            subcategory = subcategory,
        )

    private fun sampleRecommendation(): CareRecommendation =
        CareRecommendation(
            stageDescription = "desc",
            nutrition = "n",
            activity = "a",
            veterinaryCheckFrequency = "v",
            dentalCare = null,
            warningSigns = "w",
            sourceUrl = "u",
            sourceName = "s",
            disclaimer = "d",
        )

    private fun makeViewModel(
        petId: Long,
        petRepo: FakePetRepository = FakePetRepository(),
        careRepo: FakeCareRepository = FakeCareRepository(),
        settingsReader: FakeSettingsReader = FakeSettingsReader(),
        locale: String = "ru",
    ): PetDetailViewModel {
        val calculatePetAgeUseCase =
            CalculatePetAgeUseCase(
                dogAgeCalculator = DogAgeCalculator(),
                dogLifeStageCalculator = DogLifeStageCalculator(),
                catAgeCalculator = CatAgeCalculator(),
                catLifeStageCalculator = CatLifeStageCalculator(),
                settingsReader = settingsReader,
                clock = testClock,
            )
        val getCareRecommendationsUseCase = GetCareRecommendationsUseCase(careRepo)
        val savedStateHandle = SavedStateHandle(mapOf("petId" to petId))
        return PetDetailViewModel(
            savedStateHandle = savedStateHandle,
            getPetById = petRepo.getPetByIdUseCase(),
            calculatePetAge = calculatePetAgeUseCase,
            getCareRecommendations = getCareRecommendationsUseCase,
            localeProvider = FakeLocaleProvider(locale),
        )
    }

    private class FakeLocaleProvider(
        private val locale: String,
    ) : LocaleProvider() {
        override fun current(): String = locale
    }

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading before pet is fetched`() =
        runTest {
            val viewModel = makeViewModel(petId = 1L)
            viewModel.state.test {
                assertEquals(PetDetailState.Loading, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `emits NotFound when pet does not exist in repository`() =
        runTest {
            val repo = FakePetRepository()
            // Пусто.
            val viewModel = makeViewModel(petId = 42L, petRepo = repo)
            advanceUntilIdle()

            viewModel.state.test {
                assertEquals(PetDetailState.NotFound, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `emits Success with computed age and lifeStage for existing dog`() =
        runTest {
            val repo = FakePetRepository()
            val pet = samplePet(id = 1L, species = Species.Dog, subcategory = "medium")
            repo.seed(listOf(pet))

            val viewModel = makeViewModel(petId = 1L, petRepo = repo)
            advanceUntilIdle()

            viewModel.state.test {
                val state = awaitItem()
                assertIs<PetDetailState.Success>(state)
                assertEquals(pet, state.pet)
                // ageInYears ≈ 5 для собаки, рождённой 2021-05-27 и оценённой на 2026-05-27.
                assertEquals(5.0, state.calculatedAge.ageInYears, 0.1)
                assertEquals(LifeStage.Dog.MatureAdult, state.calculatedAge.lifeStage)
                // Care recommendation отсутствует — мы не seed'или его. UI рендерит null корректно.
                assertNull(state.careRecommendation)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `emits Success with care recommendation when repository has matching entry`() =
        runTest {
            val repo = FakePetRepository()
            val careRepo = FakeCareRepository()
            val pet = samplePet(id = 7L, species = Species.Cat, subcategory = "indoor_short_hair")
            repo.seed(listOf(pet))
            val rec = sampleRecommendation()
            careRepo.seed(Species.Cat, LifeStage.Cat.YoungAdult, "ru", rec)

            val viewModel = makeViewModel(petId = 7L, petRepo = repo, careRepo = careRepo, locale = "ru")
            advanceUntilIdle()

            viewModel.state.test {
                val state = awaitItem()
                assertIs<PetDetailState.Success>(state)
                assertEquals(pet, state.pet)
                assertEquals(LifeStage.Cat.YoungAdult, state.calculatedAge.lifeStage)
                assertEquals(rec, state.careRecommendation)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `emits NotFound when petId is missing in SavedStateHandle`() =
        runTest {
            // SavedStateHandle без ключа — Compose Navigation бы такого не сделал, но
            // ViewModel должен быть defense-in-depth: показать NotFound, а не упасть.
            val savedStateHandle = SavedStateHandle(emptyMap<String, Any?>())
            val petRepo = FakePetRepository()
            val careRepo = FakeCareRepository()
            val settingsReader = FakeSettingsReader()

            val calculatePetAge =
                CalculatePetAgeUseCase(
                    dogAgeCalculator = DogAgeCalculator(),
                    dogLifeStageCalculator = DogLifeStageCalculator(),
                    catAgeCalculator = CatAgeCalculator(),
                    catLifeStageCalculator = CatLifeStageCalculator(),
                    settingsReader = settingsReader,
                    clock = testClock,
                )
            val getCareRecommendations = GetCareRecommendationsUseCase(careRepo)
            val viewModel =
                PetDetailViewModel(
                    savedStateHandle = savedStateHandle,
                    getPetById = petRepo.getPetByIdUseCase(),
                    calculatePetAge = calculatePetAge,
                    getCareRecommendations = getCareRecommendations,
                    localeProvider = FakeLocaleProvider("ru"),
                )
            advanceUntilIdle()

            viewModel.state.test {
                assertEquals(PetDetailState.NotFound, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
}
