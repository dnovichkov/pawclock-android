package app.pawclock.feature.quickcalc

import app.cash.turbine.test
import app.pawclock.calculator.CatAgeCalculator
import app.pawclock.calculator.CatLifeStageCalculator
import app.pawclock.calculator.DogAgeCalculator
import app.pawclock.calculator.DogLifeStageCalculator
import app.pawclock.domain.usecase.CalculatePetAgeUseCase
import app.pawclock.feature.quickcalc.fakes.FakeSettingsReader
import app.pawclock.model.CalculationMethod
import app.pawclock.model.CatType
import app.pawclock.model.DogSize
import app.pawclock.model.LifeStage
import app.pawclock.model.Species
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
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
 * TDD-тесты для [QuickCalcViewModel] (Task 20 / Plan 1).
 *
 * Quick Calculator — одноразовый расчёт без сохранения питомца в БД (см. §3.2).
 * ViewModel держит in-memory форму (species/subcategory/birthDate/method) и
 * по событию [QuickCalcEvent.Calculate] валидирует + считает [CalculatedAge]
 * через реальный [CalculatePetAgeUseCase] (повторно используем pure-Kotlin
 * `:core:calculator` без fake'ов калькуляторов).
 *
 * Проверяемое поведение:
 *  1. Initial state — пустая форма, [QuickCalcResult.Idle].
 *  2. Calculate с валидными полями для собаки → Success с humanYears + lifeStage.
 *  3. Calculate с валидными полями для кошки → Success.
 *  4. Calculate без species → ValidationError(SpeciesRequired).
 *  5. Calculate без birthDate → ValidationError(BirthDateRequired).
 *  6. Calculate с future birthDate → ValidationError(BirthDateInFuture).
 *  7. Переключение метода Wang/SizeBased для собаки пересчитывает результат.
 *  8. Смена species очищает subcategory и result → Idle.
 *  9. Изменение формы после успешного расчёта возвращает result в Idle.
 * 10. Subcategory дефолтит к Medium (собака) / IndoorShortHair (кошка) если не указана.
 *
 * Clock.fixed гарантирует детерминизм future-date проверок на любой CI date.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class QuickCalcViewModelTest {
    private val fixedDate: LocalDate = LocalDate.of(2026, 5, 28)
    private val fixedClock: Clock =
        Clock.fixed(fixedDate.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC)

    private fun newViewModel(settingsReader: FakeSettingsReader = FakeSettingsReader()): QuickCalcViewModel {
        val calculatePetAge =
            CalculatePetAgeUseCase(
                dogAgeCalculator = DogAgeCalculator(),
                dogLifeStageCalculator = DogLifeStageCalculator(),
                catAgeCalculator = CatAgeCalculator(),
                catLifeStageCalculator = CatLifeStageCalculator(),
                settingsReader = settingsReader,
                clock = fixedClock,
            )
        return QuickCalcViewModel(calculatePetAge = calculatePetAge)
    }

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty form with Idle result`() =
        runTest {
            val viewModel = newViewModel()
            viewModel.state.test {
                val s = awaitItem()
                assertNull(s.species)
                assertNull(s.subcategory)
                assertNull(s.birthDate)
                assertEquals(CalculationMethod.EPIGENETIC, s.method)
                assertTrue(s.availableSubcategories.isEmpty())
                assertEquals(QuickCalcResult.Idle, s.result)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `selecting Dog populates subcategories and clears subcategory`() =
        runTest {
            val viewModel = newViewModel()
            viewModel.handleEvent(QuickCalcEvent.SelectSpecies(Species.Dog))

            val s = viewModel.state.value
            assertEquals(Species.Dog, s.species)
            assertEquals(
                DogSize.entries.map { it.id },
                s.availableSubcategories.map { it.id },
            )
            assertNull(s.subcategory)
            assertEquals(QuickCalcResult.Idle, s.result)
        }

    @Test
    fun `selecting Cat exposes CatType subcategories`() =
        runTest {
            val viewModel = newViewModel()
            viewModel.handleEvent(QuickCalcEvent.SelectSpecies(Species.Cat))

            val s = viewModel.state.value
            assertEquals(Species.Cat, s.species)
            assertEquals(
                CatType.entries.map { it.id },
                s.availableSubcategories.map { it.id },
            )
        }

    @Test
    fun `changing species clears previously selected subcategory`() =
        runTest {
            val viewModel = newViewModel()
            viewModel.handleEvent(QuickCalcEvent.SelectSpecies(Species.Dog))
            viewModel.handleEvent(QuickCalcEvent.SetSubcategory(DogSize.Medium.id))
            assertEquals(DogSize.Medium.id, viewModel.state.value.subcategory)

            viewModel.handleEvent(QuickCalcEvent.SelectSpecies(Species.Cat))
            assertEquals(Species.Cat, viewModel.state.value.species)
            assertNull(viewModel.state.value.subcategory)
        }

    @Test
    fun `Calculate with valid dog inputs emits Success with humanYears and lifeStage`() =
        runTest {
            val viewModel = newViewModel()
            viewModel.handleEvent(QuickCalcEvent.SelectSpecies(Species.Dog))
            viewModel.handleEvent(QuickCalcEvent.SetSubcategory(DogSize.Medium.id))
            // 2026-05-28 fixedClock; рождение 2021-05-28 → ровно 5 лет.
            viewModel.handleEvent(QuickCalcEvent.SetBirthDate(LocalDate.of(2021, 5, 28)))

            viewModel.handleEvent(QuickCalcEvent.Calculate)

            val result = viewModel.state.value.result
            assertIs<QuickCalcResult.Success>(result)
            assertEquals(5.0, result.calculatedAge.ageInYears, 0.05)
            assertEquals(LifeStage.Dog.MatureAdult, result.calculatedAge.lifeStage)
            assertEquals(CalculationMethod.EPIGENETIC, result.calculatedAge.method)
            // Wang(5) = 16·ln(5) + 31 ≈ 56.74
            assertEquals(56.74, result.calculatedAge.humanYears, 0.1)
        }

    @Test
    fun `Calculate with valid cat inputs emits Success`() =
        runTest {
            val viewModel = newViewModel()
            viewModel.handleEvent(QuickCalcEvent.SelectSpecies(Species.Cat))
            viewModel.handleEvent(QuickCalcEvent.SetSubcategory(CatType.IndoorShortHair.id))
            viewModel.handleEvent(QuickCalcEvent.SetBirthDate(LocalDate.of(2021, 5, 28)))

            viewModel.handleEvent(QuickCalcEvent.Calculate)

            val result = viewModel.state.value.result
            assertIs<QuickCalcResult.Success>(result)
            // Cat 5 лет indoor: 15 + 9 + 4·3 = 36 ЧГ
            assertEquals(36.0, result.calculatedAge.humanYears, 0.05)
            assertEquals(LifeStage.Cat.YoungAdult, result.calculatedAge.lifeStage)
        }

    @Test
    fun `Calculate without species records SpeciesRequired validation error`() =
        runTest {
            val viewModel = newViewModel()
            viewModel.handleEvent(QuickCalcEvent.SetBirthDate(LocalDate.of(2020, 1, 1)))

            viewModel.handleEvent(QuickCalcEvent.Calculate)

            val result = viewModel.state.value.result
            assertIs<QuickCalcResult.ValidationError>(result)
            assertTrue(QuickCalcValidationError.SpeciesRequired in result.errors)
        }

    @Test
    fun `Calculate without birthDate records BirthDateRequired validation error`() =
        runTest {
            val viewModel = newViewModel()
            viewModel.handleEvent(QuickCalcEvent.SelectSpecies(Species.Dog))
            viewModel.handleEvent(QuickCalcEvent.SetSubcategory(DogSize.Small.id))

            viewModel.handleEvent(QuickCalcEvent.Calculate)

            val result = viewModel.state.value.result
            assertIs<QuickCalcResult.ValidationError>(result)
            assertTrue(QuickCalcValidationError.BirthDateRequired in result.errors)
        }

    @Test
    fun `Calculate with future birthDate records BirthDateInFuture validation error`() =
        runTest {
            val viewModel = newViewModel()
            viewModel.handleEvent(QuickCalcEvent.SelectSpecies(Species.Dog))
            // fixedClock = 2026-05-28; 2027 — в будущем.
            viewModel.handleEvent(QuickCalcEvent.SetBirthDate(LocalDate.of(2027, 1, 1)))

            viewModel.handleEvent(QuickCalcEvent.Calculate)

            val result = viewModel.state.value.result
            assertIs<QuickCalcResult.ValidationError>(result)
            assertTrue(QuickCalcValidationError.BirthDateInFuture in result.errors)
        }

    @Test
    fun `switching method from Wang to SizeBased recomputes dog result`() =
        runTest {
            val viewModel = newViewModel()
            viewModel.handleEvent(QuickCalcEvent.SelectSpecies(Species.Dog))
            viewModel.handleEvent(QuickCalcEvent.SetSubcategory(DogSize.Medium.id))
            viewModel.handleEvent(QuickCalcEvent.SetBirthDate(LocalDate.of(2021, 5, 28)))
            viewModel.handleEvent(QuickCalcEvent.Calculate)

            val wangHumanYears =
                (viewModel.state.value.result as QuickCalcResult.Success).calculatedAge.humanYears
            assertEquals(CalculationMethod.EPIGENETIC, viewModel.state.value.method)

            viewModel.handleEvent(QuickCalcEvent.SetMethod(CalculationMethod.SIZE_BASED))

            // После смены метода state должен содержать SizeBased-результат БЕЗ повторного Calculate.
            val sizeBasedResult = viewModel.state.value.result
            assertIs<QuickCalcResult.Success>(sizeBasedResult)
            assertEquals(CalculationMethod.SIZE_BASED, sizeBasedResult.calculatedAge.method)
            // Medium 5y по таблице AKC/AAHA 2019 = 40 ЧГ, существенно отличается от Wang(5)≈56.7.
            assertTrue(
                kotlin.math.abs(sizeBasedResult.calculatedAge.humanYears - wangHumanYears) > 1.0,
                "SizeBased result must differ from Wang result for dog age 5 by more than 1 year",
            )
        }

    @Test
    fun `editing birthDate after Success resets result back to Idle`() =
        runTest {
            val viewModel = newViewModel()
            viewModel.handleEvent(QuickCalcEvent.SelectSpecies(Species.Dog))
            viewModel.handleEvent(QuickCalcEvent.SetSubcategory(DogSize.Medium.id))
            viewModel.handleEvent(QuickCalcEvent.SetBirthDate(LocalDate.of(2021, 5, 28)))
            viewModel.handleEvent(QuickCalcEvent.Calculate)
            assertIs<QuickCalcResult.Success>(viewModel.state.value.result)

            // Меняем дату — старый результат становится stale, ViewModel должен очистить.
            viewModel.handleEvent(QuickCalcEvent.SetBirthDate(LocalDate.of(2022, 1, 1)))
            assertEquals(QuickCalcResult.Idle, viewModel.state.value.result)
        }

    @Test
    fun `subcategory defaults to medium for dog when not specified`() =
        runTest {
            // Не передаём subcategory — CalculatePetAgeUseCase падает на null'е, поэтому
            // ViewModel должен передать дефолт (Medium/IndoorShortHair) для UX-friendly behavior.
            val viewModel = newViewModel()
            viewModel.handleEvent(QuickCalcEvent.SelectSpecies(Species.Dog))
            viewModel.handleEvent(QuickCalcEvent.SetBirthDate(LocalDate.of(2021, 5, 28)))

            viewModel.handleEvent(QuickCalcEvent.Calculate)

            val result = viewModel.state.value.result
            assertIs<QuickCalcResult.Success>(result)
            // Default Medium → Wang всё ещё используется (метод EPIGENETIC по умолчанию).
            // Если subcategory была бы null, поведение SIZE_BASED было бы сломано.
        }

    @Test
    fun `setting method on Idle state does not trigger calculation`() =
        runTest {
            val viewModel = newViewModel()
            viewModel.handleEvent(QuickCalcEvent.SelectSpecies(Species.Dog))
            viewModel.handleEvent(QuickCalcEvent.SetSubcategory(DogSize.Medium.id))
            // birthDate отсутствует → не должно быть автоматического расчёта при смене method.

            viewModel.handleEvent(QuickCalcEvent.SetMethod(CalculationMethod.SIZE_BASED))

            assertEquals(CalculationMethod.SIZE_BASED, viewModel.state.value.method)
            // Result должен остаться Idle — не было успешного предыдущего расчёта.
            assertEquals(QuickCalcResult.Idle, viewModel.state.value.result)
        }

    @Test
    fun `subcategory defaults to IndoorShortHair for cat when not specified`() =
        runTest {
            val viewModel = newViewModel()
            viewModel.handleEvent(QuickCalcEvent.SelectSpecies(Species.Cat))
            viewModel.handleEvent(QuickCalcEvent.SetBirthDate(LocalDate.of(2024, 5, 28)))

            viewModel.handleEvent(QuickCalcEvent.Calculate)

            val result = viewModel.state.value.result
            assertIs<QuickCalcResult.Success>(result)
            // Cat 2 года indoor = 15 + 9 = 24 ЧГ; outdoor добавил бы +15% после 2 лет.
            assertEquals(LifeStage.Cat.YoungAdult, result.calculatedAge.lifeStage)
        }
}
