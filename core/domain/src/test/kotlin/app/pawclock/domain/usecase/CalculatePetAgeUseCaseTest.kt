package app.pawclock.domain.usecase

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
    fun `dog with unrecognized subcategory falls back to Medium default`() =
        runTest {
            // Импорт/ручная правка могут сохранить произвольную строку подкатегории; resolveDogSize
            // тихо деградирует к DogSize.Medium. Проверяем на SIZE_BASED, где размер наблюдаем в ЧГ.
            val unknown = dog(birthDate = fixedToday.minusYears(5), subcategoryId = "banana")
            val medium = dog(birthDate = fixedToday.minusYears(5), subcategoryId = "medium")

            val unknownResult = useCase(defaultMethod = CalculationMethod.SIZE_BASED).invoke(unknown)
            val mediumResult = useCase(defaultMethod = CalculationMethod.SIZE_BASED).invoke(medium)

            assertEquals(
                mediumResult.humanYears,
                unknownResult.humanYears,
                absoluteTolerance = 0.001,
            )
            assertEquals(36.0, unknownResult.humanYears, absoluteTolerance = 0.5)
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

    // ─── Rat / Mouse (no subcategory) ──────────────────────────────────────

    @Test
    fun `rat 2 years returns approx 29 human years and Senior`() =
        runTest {
            val pet =
                Pet(
                    id = 3L,
                    name = "Splinter",
                    species = Species.Rat,
                    birthDate = fixedToday.minusYears(2),
                )
            val result = useCase().invoke(pet)
            assertEquals(2.0, result.ageInYears, absoluteTolerance = 0.01)
            assertEquals(29.0, result.humanYears, absoluteTolerance = 0.2)
            assertEquals(LifeStage.Rat.Senior, result.lifeStage)
            assertEquals(CalculationMethod.EPIGENETIC, result.method)
        }

    @Test
    fun `mouse 2 years returns approx 74 human years and Senior`() =
        runTest {
            val pet =
                Pet(
                    id = 4L,
                    name = "Jerry",
                    species = Species.Mouse,
                    birthDate = fixedToday.minusYears(2),
                )
            val result = useCase().invoke(pet)
            assertEquals(2.0, result.ageInYears, absoluteTolerance = 0.01)
            assertEquals(74.5, result.humanYears, absoluteTolerance = 0.3)
            assertEquals(LifeStage.Mouse.Senior, result.lifeStage)
            assertEquals(CalculationMethod.EPIGENETIC, result.method)
        }

    @Test
    fun `ferret 3 years returns approx 48 human years and Senior`() =
        runTest {
            val pet =
                Pet(
                    id = 5L,
                    name = "Frankie",
                    species = Species.Ferret,
                    birthDate = fixedToday.minusYears(3),
                )
            val result = useCase().invoke(pet)
            assertEquals(3.0, result.ageInYears, absoluteTolerance = 0.01)
            assertEquals(48.0, result.humanYears, absoluteTolerance = 0.2)
            assertEquals(LifeStage.Ferret.Senior, result.lifeStage)
            assertEquals(CalculationMethod.EPIGENETIC, result.method)
        }

    @Test
    fun `bird 3 years with null subcategory falls back to Budgerigar`() =
        runTest {
            val pet =
                Pet(
                    id = 6L,
                    name = "Kesha",
                    species = Species.Bird,
                    birthDate = fixedToday.minusYears(3),
                )
            val result = useCase().invoke(pet)
            assertEquals(3.0, result.ageInYears, absoluteTolerance = 0.01)
            // Budgerigar ЧЖ 7: 3·80/7 ≈ 34.3; доля 3/7 ≈ 0.43 → Adult
            assertEquals(34.29, result.humanYears, absoluteTolerance = 0.2)
            assertEquals(LifeStage.Bird.Adult, result.lifeStage)
            assertEquals(CalculationMethod.EPIGENETIC, result.method)
        }

    @Test
    fun `bird honours macaw subcategory for slower ageing`() =
        runTest {
            val pet =
                Pet(
                    id = 7L,
                    name = "Rio",
                    species = Species.Bird,
                    subcategory = "macaw",
                    birthDate = fixedToday.minusYears(15),
                )
            val result = useCase().invoke(pet)
            // Macaw ЧЖ 50: 15·80/50 = 24; доля 15/50 = 0.30 → Adult
            assertEquals(24.0, result.humanYears, absoluteTolerance = 0.1)
            assertEquals(LifeStage.Bird.Adult, result.lifeStage)
        }

    @Test
    fun `reptile 6 years with null subcategory falls back to BeardedDragon`() =
        runTest {
            val pet =
                Pet(
                    id = 8L,
                    name = "Spike",
                    species = Species.Reptile,
                    birthDate = fixedToday.minusYears(6),
                )
            val result = useCase().invoke(pet)
            assertEquals(6.0, result.ageInYears, absoluteTolerance = 0.01)
            // BeardedDragon ЧЖ 12: 6·80/12 = 40; доля 6/12 = 0.50 → Adult
            assertEquals(40.0, result.humanYears, absoluteTolerance = 0.2)
            assertEquals(LifeStage.Reptile.Adult, result.lifeStage)
            assertEquals(CalculationMethod.EPIGENETIC, result.method)
        }

    @Test
    fun `reptile honours box turtle subcategory for slower ageing`() =
        runTest {
            val pet =
                Pet(
                    id = 9L,
                    name = "Shelly",
                    species = Species.Reptile,
                    subcategory = "box_turtle",
                    birthDate = fixedToday.minusYears(35),
                )
            val result = useCase().invoke(pet)
            // BoxTurtle ЧЖ 40: 35·80/40 = 70; доля 35/40 = 0.875 → Senior
            // (35 лет — заведомо внутри полосы Senior, в отличие от граничных 30/40 = 0.75)
            assertEquals(70.0, result.humanYears, absoluteTolerance = 0.2)
            assertEquals(LifeStage.Reptile.Senior, result.lifeStage)
        }

    @Test
    fun `horse 10 years with null subcategory falls back to LightHorse`() =
        runTest {
            val pet =
                Pet(
                    id = 10L,
                    name = "Spirit",
                    species = Species.Horse,
                    birthDate = fixedToday.minusYears(10),
                )
            val result = useCase().invoke(pet)
            assertEquals(10.0, result.ageInYears, absoluteTolerance = 0.01)
            // AAEP: 20.5 + 2.5·6 = 35.5; 10 лет в полосе Adult (4–15)
            assertEquals(35.5, result.humanYears, absoluteTolerance = 0.2)
            assertEquals(LifeStage.Horse.Adult, result.lifeStage)
            assertEquals(CalculationMethod.EPIGENETIC, result.method)
        }

    @Test
    fun `horse 20 years pony subcategory is Senior`() =
        runTest {
            val pet =
                Pet(
                    id = 11L,
                    name = "Thunder",
                    species = Species.Horse,
                    subcategory = "pony",
                    birthDate = fixedToday.minusYears(20),
                )
            val result = useCase().invoke(pet)
            // AAEP формула не зависит от породы: 20.5 + 2.5·16 = 60.5
            assertEquals(60.5, result.humanYears, absoluteTolerance = 0.2)
            assertEquals(LifeStage.Horse.Senior, result.lifeStage)
        }

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
    fun `fish 5 years with null subcategory falls back to Goldfish`() =
        runTest {
            val pet =
                Pet(
                    id = 12L,
                    name = "Nemo",
                    species = Species.Fish,
                    birthDate = fixedToday.minusYears(5),
                )
            val result = useCase().invoke(pet)
            assertEquals(5.0, result.ageInYears, absoluteTolerance = 0.01)
            // Goldfish ЧЖ 15: 5·80/15 ≈ 26.67; доля 5/15 ≈ 0.33 → Adult
            assertEquals(26.67, result.humanYears, absoluteTolerance = 0.2)
            assertEquals(LifeStage.Fish.Adult, result.lifeStage)
            assertEquals(CalculationMethod.EPIGENETIC, result.method)
        }

    @Test
    fun `fish honours koi subcategory for slower ageing`() =
        runTest {
            val pet =
                Pet(
                    id = 13L,
                    name = "Splash",
                    species = Species.Fish,
                    subcategory = "koi",
                    birthDate = fixedToday.minusYears(27),
                )
            val result = useCase().invoke(pet)
            // Koi ЧЖ 30: 27·80/30 = 72; доля 27/30 = 0.90 → Senior
            assertEquals(72.0, result.humanYears, absoluteTolerance = 0.2)
            assertEquals(LifeStage.Fish.Senior, result.lifeStage)
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
