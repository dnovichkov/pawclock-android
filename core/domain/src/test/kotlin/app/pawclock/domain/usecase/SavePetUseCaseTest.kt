package app.pawclock.domain.usecase

import app.pawclock.domain.fakes.FakePetRepository
import app.pawclock.domain.pet.PetValidationError
import app.pawclock.domain.pet.PetValidationException
import app.pawclock.domain.pet.UnsupportedSpeciesException
import app.pawclock.model.Pet
import app.pawclock.model.Species
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest

class SavePetUseCaseTest {
    private val today: LocalDate = LocalDate.of(2026, 5, 27)
    private val clock: Clock =
        Clock.fixed(
            today.atStartOfDay(ZoneOffset.UTC).toInstant(),
            ZoneOffset.UTC,
        )

    private fun useCase(repo: FakePetRepository = FakePetRepository()): SavePetUseCase = SavePetUseCase(repo, clock)

    private fun validPet(
        id: Long = 0L,
        name: String = "Rex",
        birthDate: LocalDate = today.minusYears(3),
    ): Pet =
        Pet(
            id = id,
            name = name,
            species = Species.Dog,
            birthDate = birthDate,
            subcategory = "medium",
        )

    @Test
    fun `insert assigns auto-generated id when pet id is zero`() =
        runTest {
            val repo = FakePetRepository()
            val savedId = useCase(repo).invoke(validPet(id = 0L))
            assertEquals(1L, savedId)
            assertNotNull(repo.getById(1L))
        }

    @Test
    fun `update preserves id and modifies fields`() =
        runTest {
            val repo = FakePetRepository()
            val initialId = useCase(repo).invoke(validPet(id = 0L, name = "Rex"))
            val updatedId =
                useCase(repo).invoke(
                    validPet(id = initialId, name = "RexRenamed"),
                )
            assertEquals(initialId, updatedId)
            assertEquals("RexRenamed", repo.getById(initialId)?.name)
        }

    @Test
    fun `birthDate in future throws BirthDateInFuture`() =
        runTest {
            val pet = validPet(birthDate = today.plusDays(1))
            val ex =
                assertFailsWith<PetValidationException> { useCase().invoke(pet) }
            assertContains(ex.errors, PetValidationError.BirthDateInFuture)
        }

    @Test
    fun `birthDate before 1990 throws BirthDateUnrealistic`() =
        runTest {
            val pet = validPet(birthDate = LocalDate.of(1985, 1, 1))
            val ex =
                assertFailsWith<PetValidationException> { useCase().invoke(pet) }
            assertContains(ex.errors, PetValidationError.BirthDateUnrealistic)
        }

    @Test
    fun `unsupported species throws UnsupportedSpeciesException`() =
        runTest {
            val rabbit =
                Pet(
                    id = 0L,
                    name = "Bunny",
                    species = Species.Rabbit,
                    birthDate = today.minusYears(2),
                )
            val ex =
                assertFailsWith<UnsupportedSpeciesException> { useCase().invoke(rabbit) }
            assertEquals(Species.Rabbit, ex.species)
        }

    @Test
    fun `PetValidationException can carry multiple errors`() {
        // Multiple-error случай в SavePetUseCase сейчас недостижим через Pet-конструктор
        // (init блокирует blank name; BirthDateInFuture исключает BirthDateUnrealistic).
        // Но PetValidationException и buildList-аккумулятор готовы к этому на случай,
        // если future Pet получит новый optional field (например, weightKg < 0). Документируем
        // через прямую конструкцию исключения.
        val ex =
            PetValidationException(
                errors =
                    listOf(
                        PetValidationError.BirthDateInFuture,
                        PetValidationError.BirthDateUnrealistic,
                    ),
            )
        assertEquals(2, ex.errors.size)
        assertContains(ex.errors, PetValidationError.BirthDateInFuture)
        assertContains(ex.errors, PetValidationError.BirthDateUnrealistic)
    }

    @Test
    fun `Pet model rejects blank name at construction time`() {
        // Дополнительная defence-in-depth проверка: Pet's init блок enforce'ит .isNotBlank().
        // Это значит, что NameBlank в SavePetUseCase сработает только для случаев,
        // когда конструктор Pet был обойдён (например, через рефлексию или Room mapper).
        // Документируем поведение тестом.
        val ex =
            assertFailsWith<IllegalArgumentException> {
                Pet(
                    id = 0L,
                    name = " ",
                    species = Species.Dog,
                    birthDate = today.minusYears(2),
                    subcategory = "medium",
                )
            }
        assertEquals("Pet name must not be blank", ex.message)
    }

    @Test
    fun `validation passes for valid pet`() =
        runTest {
            val repo = FakePetRepository()
            val savedId = useCase(repo).invoke(validPet())
            assertNotNull(repo.getById(savedId))
        }

    @Test
    fun `PetValidationException requires non-empty errors`() {
        assertFailsWith<IllegalArgumentException> {
            PetValidationException(errors = emptyList())
        }
    }
}
