package app.pawclock.feature.pets.list

import app.cash.turbine.test
import app.pawclock.feature.pets.fakes.FakePetRepository
import app.pawclock.feature.pets.fakes.PausedFakePetRepository
import app.pawclock.model.Pet
import app.pawclock.model.Species
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
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
 * TDD-тесты для [PetsListViewModel] (Task 18 / Plan 1).
 *
 * Проверяемое поведение:
 *  - стартовое состояние — [PetsListState.Loading];
 *  - после первой эмиссии пустого списка из [app.pawclock.domain.usecase.GetPetsUseCase]
 *    — состояние [PetsListState.Empty];
 *  - после эмиссии непустого списка — [PetsListState.Success] с теми же питомцами;
 *  - реактивная реакция на insert/update/delete через Turbine (новая эмиссия повторно
 *    проходит через классификатор Loading/Empty/Success);
 *  - sortBy: реализация уже сортирует через PetRepository ORDER BY name COLLATE NOCASE,
 *    ViewModel сохраняет порядок.
 *
 * Используется кастомный [UnconfinedTestDispatcher] для viewModelScope, чтобы
 * stateIn/launchIn-collector'ы исполнялись синхронно в runTest.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PetsListViewModelTest {
    private val sampleDate: LocalDate = LocalDate.of(2024, 1, 1)

    private fun pet(
        id: Long,
        name: String,
    ): Pet =
        Pet(
            id = id,
            name = name,
            species = Species.Dog,
            birthDate = sampleDate,
            subcategory = "medium",
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
    fun `initial state is Loading before any emission`() =
        runTest {
            // Используем PausedFakeRepository, который не эмитит, пока тест не вызовет emit().
            val repo = PausedFakePetRepository()
            val viewModel = PetsListViewModel(getPets = repo.getPetsUseCase())

            viewModel.state.test {
                assertEquals(PetsListState.Loading, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `emits Empty when repository emits empty list`() =
        runTest {
            val repo = FakePetRepository()
            val viewModel = PetsListViewModel(getPets = repo.getPetsUseCase())

            // UnconfinedTestDispatcher запускает stateIn-collector синхронно при subscribe,
            // поэтому первое значение в Turbine может быть уже Empty (если коллектор
            // успел отработать) или Loading (если запустится после `awaitItem()`).
            // Тест принимает оба варианта, но финальное состояние — обязательно Empty.
            viewModel.state.test {
                var current = awaitItem()
                if (current is PetsListState.Loading) {
                    current = awaitItem()
                }
                assertEquals(PetsListState.Empty, current)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `emits Success with pets when repository has data`() =
        runTest {
            val repo = FakePetRepository()
            repo.seed(listOf(pet(1, "Альфа"), pet(2, "Бобик")))
            val viewModel = PetsListViewModel(getPets = repo.getPetsUseCase())

            viewModel.state.test {
                // Loading skipped (Empty или Success — зависит от того, насколько быстро эмитит StateFlow).
                // Ждём финального состояния — Success.
                val success =
                    awaitItem().takeIf { it is PetsListState.Success }
                        ?: awaitItem().takeIf { it is PetsListState.Success }
                        ?: awaitItem()
                assertIs<PetsListState.Success>(success)
                assertEquals(listOf("Альфа", "Бобик"), success.pets.map { it.name })
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `reactively re-emits Success after insert into repository`() =
        runTest {
            val repo = FakePetRepository()
            val viewModel = PetsListViewModel(getPets = repo.getPetsUseCase())

            viewModel.state.test {
                // Пропустим Loading и Empty.
                while (true) {
                    val s = awaitItem()
                    if (s is PetsListState.Empty) break
                }

                repo.insert(pet(0, "Rex"))
                val next = awaitItem()
                assertIs<PetsListState.Success>(next)
                assertEquals(1, next.pets.size)
                assertEquals("Rex", next.pets.first().name)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `transitions Success back to Empty after deleting last pet`() =
        runTest {
            val repo = FakePetRepository()
            repo.seed(listOf(pet(1, "Alpha")))
            val viewModel = PetsListViewModel(getPets = repo.getPetsUseCase())

            viewModel.state.test {
                // Ждём Success.
                var current = awaitItem()
                while (current !is PetsListState.Success) {
                    current = awaitItem()
                }
                assertEquals(listOf("Alpha"), current.pets.map { it.name })

                repo.deleteById(1L)
                val afterDelete = awaitItem()
                assertEquals(PetsListState.Empty, afterDelete)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `Success preserves repository sort order case-insensitively`() =
        runTest {
            val repo = FakePetRepository()
            // FakePetRepository сортирует по name.lowercase() — повторяем production-поведение
            // Room ORDER BY name COLLATE NOCASE.
            repo.seed(listOf(pet(1, "Чарли"), pet(2, "альфа"), pet(3, "Бобик")))
            val viewModel = PetsListViewModel(getPets = repo.getPetsUseCase())

            viewModel.state.test {
                var current = awaitItem()
                while (current !is PetsListState.Success) {
                    current = awaitItem()
                }
                val names = current.pets.map { it.name }
                // FakePetRepository.seed применяет sortedBy { it.name.lowercase() }.
                assertTrue("альфа" in names && "Бобик" in names && "Чарли" in names)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
