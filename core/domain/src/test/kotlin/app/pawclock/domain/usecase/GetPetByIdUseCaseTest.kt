package app.pawclock.domain.usecase

import app.pawclock.domain.fakes.FakePetRepository
import app.pawclock.model.Pet
import app.pawclock.model.Species
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class GetPetByIdUseCaseTest {
    private val sampleDate: LocalDate = LocalDate.of(2024, 1, 1)

    @Test
    fun `returns pet when id exists`() =
        runTest {
            val repo = FakePetRepository()
            val rex =
                Pet(
                    id = 42L,
                    name = "Rex",
                    species = Species.Dog,
                    birthDate = sampleDate,
                    subcategory = "medium",
                )
            repo.seed(listOf(rex))
            val useCase = GetPetByIdUseCase(repo)
            assertEquals(rex, useCase(42L))
        }

    @Test
    fun `returns null when id missing`() =
        runTest {
            val repo = FakePetRepository()
            val useCase = GetPetByIdUseCase(repo)
            assertNull(useCase(404L))
        }
}
