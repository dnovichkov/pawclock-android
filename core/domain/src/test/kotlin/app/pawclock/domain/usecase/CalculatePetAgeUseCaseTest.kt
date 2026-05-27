package app.pawclock.domain.usecase

import app.pawclock.calculator.CatAgeCalculator
import app.pawclock.calculator.CatLifeStageCalculator
import app.pawclock.calculator.DogAgeCalculator
import app.pawclock.calculator.DogLifeStageCalculator
import app.pawclock.domain.fakes.FakeSettingsReader
import app.pawclock.model.CalculationMethod
import app.pawclock.model.LifeStage
import app.pawclock.model.Pet
import app.pawclock.model.Species
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * Тесты [CalculatePetAgeUseCase] (Task 15).
 *
 * Используют `Clock.fixed(...)` чтобы дата в тестах была детерминированной;
 * никакого `LocalDate.now()` или `Thread.sleep` — см. §11.1 спецификации.
 */
class CalculatePetAgeUseCaseTest {
    private val fixedToday: LocalDate = LocalDate.of(2026, 5, 27)
    private val fixedClock: Clock =
        Clock.fixed(
            fixedToday.atStartOfDay(ZoneOffset.UTC).toInstant(),
            ZoneOffset.UTC,
        )

    private fun useCase(defaultMethod: CalculationMethod = CalculationMethod.EPIGENETIC): CalculatePetAgeUseCase =
        CalculatePetAgeUseCase(
            dogAgeCalculator = DogAgeCalculator(),
            dogLifeStageCalculator = DogLifeStageCalculator(),
            catAgeCalculator = CatAgeCalculator(),
            catLifeStageCalculator = CatLifeStageCalculator(),
            settingsReader = FakeSettingsReader(defaultMethod),
            clock = fixedClock,
        )

    private fun dog(
        birthDate: LocalDate,
        subcategoryId: String? = "medium",
    ): Pet =
        Pet(
            id = 1L,
            name = "Rex",
            species = Species.Dog,
            birthDate = birthDate,
            subcategory = subcategoryId,
        )

    private fun cat(
        birthDate: LocalDate,
        subcategoryId: String? = "indoor_short_hair",
    ): Pet =
        Pet(
            id = 2L,
            name = "Felix",
            species = Species.Cat,
            birthDate = birthDate,
            subcategory = subcategoryId,
        )

    // ─── Dog: EPIGENETIC ───────────────────────────────────────────────────

    @Test
    fun `dog 5 years EPIGENETIC returns approx 57 human years and MatureAdult`() =
        runTest {
            val pet = dog(birthDate = fixedToday.minusYears(5))
            val result = useCase().invoke(pet)
            assertEquals(5.0, result.ageInYears, absoluteTolerance = 0.01)
            assertEquals(56.7, result.humanYears, absoluteTolerance = 0.2)
            assertEquals(LifeStage.Dog.MatureAdult, result.lifeStage)
            assertEquals(CalculationMethod.EPIGENETIC, result.method)
        }

    @Test
    fun `dog 1 year EPIGENETIC returns 31 human years`() =
        runTest {
            val pet = dog(birthDate = fixedToday.minusYears(1))
            val result = useCase().invoke(pet)
            assertEquals(31.0, result.humanYears, absoluteTolerance = 0.1)
        }

    // ─── Dog: SIZE_BASED ───────────────────────────────────────────────────

    @Test
    fun `dog 5 years SIZE_BASED Medium uses size-based table`() =
        runTest {
            val pet = dog(birthDate = fixedToday.minusYears(5), subcategoryId = "medium")
            val result = useCase(defaultMethod = CalculationMethod.SIZE_BASED).invoke(pet)
            assertEquals(CalculationMethod.SIZE_BASED, result.method)
            // Medium 5 лет по AKC/AAHA таблице — 36 ЧГ
            assertEquals(36.0, result.humanYears, absoluteTolerance = 0.5)
        }

    @Test
    fun `methodOverride wins over settings`() =
        runTest {
            val pet = dog(birthDate = fixedToday.minusYears(5))
            // Дефолт — SIZE_BASED, но override на EPIGENETIC
            val result =
                useCase(defaultMethod = CalculationMethod.SIZE_BASED)
                    .invoke(pet, methodOverride = CalculationMethod.EPIGENETIC)
            assertEquals(CalculationMethod.EPIGENETIC, result.method)
            assertEquals(56.7, result.humanYears, absoluteTolerance = 0.2)
        }

    @Test
    fun `dog with null subcategory falls back to Medium`() =
        runTest {
            val pet = dog(birthDate = fixedToday.minusYears(5), subcategoryId = null)
            val result = useCase().invoke(pet)
            // Без subcategory собака считается как Medium; для EPIGENETIC размер не влияет
            // на humanYears (Wang-формула не учитывает размер), поэтому ≈ 56.7 ЧГ.
            assertEquals(56.7, result.humanYears, absoluteTolerance = 0.2)
            // Medium собака 5 лет → MatureAdult по AAHA 2019 (см. DogLifeStageThresholds)
            assertEquals(LifeStage.Dog.MatureAdult, result.lifeStage)
        }

    // ─── Dog: life stage by size ──────────────────────────────────────────

    @Test
    fun `giant dog 6 years is Senior`() =
        runTest {
            val pet = dog(birthDate = fixedToday.minusYears(6), subcategoryId = "giant")
            val result = useCase().invoke(pet)
            assertEquals(LifeStage.Dog.Senior, result.lifeStage)
        }

    @Test
    fun `toy dog 16 years is EndOfLife`() =
        runTest {
            val pet = dog(birthDate = fixedToday.minusYears(16), subcategoryId = "toy")
            val result = useCase().invoke(pet)
            assertEquals(LifeStage.Dog.EndOfLife, result.lifeStage)
        }

    // ─── Cat: AAFP 2021 ───────────────────────────────────────────────────

    @Test
    fun `cat 5 years indoor returns 36 human years and YoungAdult`() =
        runTest {
            val pet = cat(birthDate = fixedToday.minusYears(5))
            val result = useCase().invoke(pet)
            assertEquals(36.0, result.humanYears, absoluteTolerance = 0.01)
            assertEquals(LifeStage.Cat.YoungAdult, result.lifeStage)
            // У кошек method фиксирован, см. KDoc CalculatedAge.
            assertEquals(CalculationMethod.EPIGENETIC, result.method)
        }

    @Test
    fun `cat 5 years outdoor returns 41-point-4 human years`() =
        runTest {
            val pet = cat(birthDate = fixedToday.minusYears(5), subcategoryId = "outdoor")
            val result = useCase().invoke(pet)
            assertEquals(41.4, result.humanYears, absoluteTolerance = 0.01)
        }

    @Test
    fun `cat 5 years large_breed returns 39 human years`() =
        runTest {
            val pet = cat(birthDate = fixedToday.minusYears(5), subcategoryId = "large_breed")
            val result = useCase().invoke(pet)
            assertEquals(39.0, result.humanYears, absoluteTolerance = 0.01)
        }

    @Test
    fun `cat 12 years is Senior`() =
        runTest {
            val pet = cat(birthDate = fixedToday.minusYears(12))
            val result = useCase().invoke(pet)
            assertEquals(LifeStage.Cat.Senior, result.lifeStage)
        }

    @Test
    fun `cat with null subcategory falls back to IndoorShortHair`() =
        runTest {
            val pet = cat(birthDate = fixedToday.minusYears(5), subcategoryId = null)
            val result = useCase().invoke(pet)
            // Same humanYears как у indoor_short_hair = 36 ЧГ
            assertEquals(36.0, result.humanYears, absoluteTolerance = 0.01)
        }

    // ─── Errors ───────────────────────────────────────────────────────────

    @Test
    fun `birthDate in future throws IllegalArgumentException`() =
        runTest {
            val pet = dog(birthDate = fixedToday.plusDays(1))
            assertFailsWith<IllegalArgumentException> { useCase().invoke(pet) }
        }

    @Test
    fun `same-day birth throws because ageInYears is zero`() =
        runTest {
            val pet = dog(birthDate = fixedToday)
            assertFailsWith<IllegalArgumentException> { useCase().invoke(pet) }
        }

    @Test
    fun `unsupported species throws UnsupportedSpeciesException`() =
        runTest {
            val pet =
                Pet(
                    id = 1L,
                    name = "Bunny",
                    species = Species.Rabbit,
                    birthDate = fixedToday.minusYears(2),
                )
            val ex =
                assertFailsWith<app.pawclock.domain.pet.UnsupportedSpeciesException> {
                    useCase().invoke(pet)
                }
            assertEquals(Species.Rabbit, ex.species)
        }

    // ─── Calendar age computation ─────────────────────────────────────────

    @Test
    fun `ageInYears uses 365-point-25 days per year`() =
        runTest {
            // Ровно 365 дней = 365/365.25 ≈ 0.9993 года < 1 → попадает в puppy-ветку Wang
            val pet = dog(birthDate = fixedToday.minusDays(365))
            val result = useCase().invoke(pet)
            assertEquals(365.0 / 365.25, result.ageInYears, absoluteTolerance = 1e-6)
            assertTrue(result.ageInYears < 1.0, "expected ageInYears < 1.0, got ${result.ageInYears}")
        }
}
