package app.pawclock.feature.editor

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import app.pawclock.domain.pet.PetValidationError
import app.pawclock.feature.editor.fakes.FakePetRepository
import app.pawclock.model.CatType
import app.pawclock.model.DogSize
import app.pawclock.model.Gender
import app.pawclock.model.Pet
import app.pawclock.model.Species
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

/**
 * TDD-тесты для [PetEditorViewModel] (Task 19 / Plan 1).
 *
 * Проверяемое поведение:
 *  - выбор Species обновляет state и пополняет список доступных подкатегорий
 *    (Dog → [DogSize.entries], Cat → [CatType.entries]);
 *  - смена Species очищает выбранную subcategory;
 *  - событие Save без имени → state.validationErrors содержит NameBlank;
 *  - событие Save с birthDate в будущем → state.validationErrors содержит BirthDateInFuture;
 *  - валидный Save вызывает SavePetUseCase и переводит state.saveResult в Success;
 *  - режим редактирования (petId в SavedStateHandle) загружает Pet и заполняет state.
 *
 * `Clock.fixed` гарантирует детерминированную проверку future-dates на CI с любой системной датой.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PetEditorViewModelTest {
    private val fixedDate: LocalDate = LocalDate.of(2026, 5, 28)
    private val fixedClock: Clock =
        Clock.fixed(fixedDate.atStartOfDay(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"))

    private fun newViewModel(
        petId: Long? = null,
        repo: FakePetRepository = FakePetRepository(),
    ): PetEditorViewModel =
        PetEditorViewModel(
            savedStateHandle =
                SavedStateHandle(
                    if (petId == null) emptyMap() else mapOf("petId" to petId),
                ),
            getPetById = repo.getPetByIdUseCase(),
            savePet = repo.savePetUseCase(clock = fixedClock),
        )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty form when no petId in SavedStateHandle`() =
        runTest {
            val viewModel = newViewModel(petId = null)
            viewModel.state.test {
                val s = awaitItem()
                assertEquals("", s.name)
                assertNull(s.species)
                assertNull(s.subcategory)
                assertNull(s.birthDate)
                assertNull(s.gender)
                assertEquals("", s.weightKg)
                assertEquals("", s.notes)
                assertFalse(s.isSaving)
                assertNull(s.saveResult)
                assertTrue(s.validationErrors.isEmpty())
                assertTrue(s.availableSubcategories.isEmpty())
                assertNull(s.editingPetId)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `selecting Dog species exposes DogSize subcategories`() =
        runTest {
            val viewModel = newViewModel()
            viewModel.handleEvent(PetEditorEvent.SelectSpecies(Species.Dog))

            viewModel.state.test {
                val s = awaitItem()
                assertEquals(Species.Dog, s.species)
                assertEquals(
                    DogSize.entries.map { it.id },
                    s.availableSubcategories.map { it.id },
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `selecting Cat species exposes CatType subcategories`() =
        runTest {
            val viewModel = newViewModel()
            viewModel.handleEvent(PetEditorEvent.SelectSpecies(Species.Cat))

            viewModel.state.test {
                val s = awaitItem()
                assertEquals(Species.Cat, s.species)
                assertEquals(
                    CatType.entries.map { it.id },
                    s.availableSubcategories.map { it.id },
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `changing species clears previously selected subcategory`() =
        runTest {
            val viewModel = newViewModel()
            viewModel.handleEvent(PetEditorEvent.SelectSpecies(Species.Dog))
            viewModel.handleEvent(PetEditorEvent.SetSubcategory(DogSize.Medium.id))
            assertEquals(DogSize.Medium.id, viewModel.state.value.subcategory)

            viewModel.handleEvent(PetEditorEvent.SelectSpecies(Species.Cat))
            viewModel.state.test {
                val s = awaitItem()
                assertEquals(Species.Cat, s.species)
                assertNull(s.subcategory)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `saving with blank name records NameBlank validation error`() =
        runTest {
            val viewModel = newViewModel()
            viewModel.handleEvent(PetEditorEvent.SelectSpecies(Species.Dog))
            viewModel.handleEvent(PetEditorEvent.SetSubcategory(DogSize.Medium.id))
            viewModel.handleEvent(PetEditorEvent.SetBirthDate(LocalDate.of(2020, 1, 1)))
            // name остаётся пустым.

            viewModel.handleEvent(PetEditorEvent.Save)

            val s = viewModel.state.value
            assertTrue(PetValidationError.NameBlank in s.validationErrors)
            assertNull(s.saveResult)
        }

    @Test
    fun `saving with future birthDate records BirthDateInFuture error`() =
        runTest {
            val viewModel = newViewModel()
            viewModel.handleEvent(PetEditorEvent.SelectSpecies(Species.Dog))
            viewModel.handleEvent(PetEditorEvent.SetSubcategory(DogSize.Medium.id))
            viewModel.handleEvent(PetEditorEvent.SetName("Rex"))
            // fixedClock = 2026-05-28; ставим 2027 = в будущем.
            viewModel.handleEvent(PetEditorEvent.SetBirthDate(LocalDate.of(2027, 1, 1)))

            viewModel.handleEvent(PetEditorEvent.Save)

            val s = viewModel.state.value
            assertTrue(PetValidationError.BirthDateInFuture in s.validationErrors)
            assertNull(s.saveResult)
        }

    @Test
    fun `valid save calls SavePetUseCase and emits SaveResult Success`() =
        runTest {
            val repo = FakePetRepository()
            val viewModel = newViewModel(petId = null, repo = repo)
            viewModel.handleEvent(PetEditorEvent.SelectSpecies(Species.Dog))
            viewModel.handleEvent(PetEditorEvent.SetSubcategory(DogSize.Medium.id))
            viewModel.handleEvent(PetEditorEvent.SetName("Rex"))
            viewModel.handleEvent(PetEditorEvent.SetBirthDate(LocalDate.of(2020, 1, 1)))
            viewModel.handleEvent(PetEditorEvent.SetGender(Gender.Male))
            viewModel.handleEvent(PetEditorEvent.SetWeight("12.5"))
            viewModel.handleEvent(PetEditorEvent.SetNotes("playful"))

            viewModel.handleEvent(PetEditorEvent.Save)

            val s = viewModel.state.value
            val result = s.saveResult
            assertNotNull(result)
            assertIs<PetEditorSaveResult.Success>(result)
            assertTrue(result.petId >= 1L)
            assertTrue(s.validationErrors.isEmpty())

            // Проверяем, что Pet реально сохранён в репозитории.
            val saved = repo.getPetByIdUseCase()(result.petId)
            assertNotNull(saved)
            assertEquals("Rex", saved.name)
            assertEquals(Species.Dog, saved.species)
            assertEquals(DogSize.Medium.id, saved.subcategory)
            assertEquals(Gender.Male, saved.gender)
            assertEquals(12.5, saved.weightKg)
            assertEquals("playful", saved.notes)
        }

    @Test
    fun `weight with invalid number is dropped and pet saved with null weightKg`() =
        runTest {
            val repo = FakePetRepository()
            val viewModel = newViewModel(petId = null, repo = repo)
            viewModel.handleEvent(PetEditorEvent.SelectSpecies(Species.Dog))
            viewModel.handleEvent(PetEditorEvent.SetSubcategory(DogSize.Small.id))
            viewModel.handleEvent(PetEditorEvent.SetName("Бобик"))
            viewModel.handleEvent(PetEditorEvent.SetBirthDate(LocalDate.of(2021, 6, 1)))
            viewModel.handleEvent(PetEditorEvent.SetWeight("not-a-number"))

            viewModel.handleEvent(PetEditorEvent.Save)

            val result = viewModel.state.value.saveResult
            assertIs<PetEditorSaveResult.Success>(result)
            val saved = repo.getPetByIdUseCase()(result.petId)
            assertNotNull(saved)
            assertNull(saved.weightKg)
        }

    @Test
    fun `editing existing pet populates state from repository on init`() =
        runTest {
            val repo = FakePetRepository()
            repo.insert(
                Pet(
                    id = 0L,
                    name = "Барсик",
                    species = Species.Cat,
                    birthDate = LocalDate.of(2020, 3, 15),
                    subcategory = CatType.IndoorShortHair.id,
                    gender = Gender.Female,
                    weightKg = 4.2,
                    notes = "ласковая",
                ),
            )
            val savedPetId = repo.observeAll().let { _ -> 1L }

            val viewModel = newViewModel(petId = savedPetId, repo = repo)

            viewModel.state.test {
                // UnconfinedTestDispatcher запустит init { loadPet(...) } синхронно.
                // Дождёмся, пока state будет заполнен (имя != "" — индикатор).
                var s = awaitItem()
                while (s.name.isEmpty() && !s.isLoading.not()) {
                    s = awaitItem()
                }
                // На UnconfinedTestDispatcher первая эмиссия уже должна нести загруженные данные.
                if (s.name.isEmpty()) s = awaitItem()
                assertEquals("Барсик", s.name)
                assertEquals(Species.Cat, s.species)
                assertEquals(CatType.IndoorShortHair.id, s.subcategory)
                assertEquals(LocalDate.of(2020, 3, 15), s.birthDate)
                assertEquals(Gender.Female, s.gender)
                assertEquals("4.2", s.weightKg)
                assertEquals("ласковая", s.notes)
                assertEquals(savedPetId, s.editingPetId)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `updating existing pet preserves id`() =
        runTest {
            val repo = FakePetRepository()
            val originalId =
                repo.insert(
                    Pet(
                        id = 0L,
                        name = "Old",
                        species = Species.Dog,
                        birthDate = LocalDate.of(2019, 1, 1),
                        subcategory = DogSize.Large.id,
                    ),
                )
            val viewModel = newViewModel(petId = originalId, repo = repo)

            // Ждём загрузки.
            viewModel.state.test {
                var s = awaitItem()
                while (s.editingPetId == null) s = awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.handleEvent(PetEditorEvent.SetName("Renamed"))
            viewModel.handleEvent(PetEditorEvent.Save)

            val result = viewModel.state.value.saveResult
            assertIs<PetEditorSaveResult.Success>(result)
            assertEquals(originalId, result.petId)
            val updated = repo.getPetByIdUseCase()(originalId)
            assertEquals("Renamed", updated?.name)
        }

    @Test
    fun `Save event without species records validation error and does not call use case`() =
        runTest {
            val viewModel = newViewModel()
            viewModel.handleEvent(PetEditorEvent.SetName("NoSpecies"))
            viewModel.handleEvent(PetEditorEvent.SetBirthDate(LocalDate.of(2021, 1, 1)))

            viewModel.handleEvent(PetEditorEvent.Save)

            val s = viewModel.state.value
            assertNull(s.saveResult)
            assertTrue(s.formErrorMessageKey != null)
        }

    @Test
    fun `consuming save result clears it from state`() =
        runTest {
            val repo = FakePetRepository()
            val viewModel = newViewModel(petId = null, repo = repo)
            viewModel.handleEvent(PetEditorEvent.SelectSpecies(Species.Dog))
            viewModel.handleEvent(PetEditorEvent.SetSubcategory(DogSize.Medium.id))
            viewModel.handleEvent(PetEditorEvent.SetName("Rex"))
            viewModel.handleEvent(PetEditorEvent.SetBirthDate(LocalDate.of(2020, 1, 1)))
            viewModel.handleEvent(PetEditorEvent.Save)

            assertNotNull(viewModel.state.value.saveResult)
            viewModel.handleEvent(PetEditorEvent.ConsumeSaveResult)
            assertNull(viewModel.state.value.saveResult)
        }
}
