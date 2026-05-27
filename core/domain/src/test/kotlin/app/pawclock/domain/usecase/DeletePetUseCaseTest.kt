package app.pawclock.domain.usecase

import app.pawclock.domain.fakes.FakePetRepository
import app.pawclock.model.Pet
import app.pawclock.model.Species
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class DeletePetUseCaseTest {
    private val sampleDate: LocalDate = LocalDate.of(2024, 1, 1)

    @Test
    fun `returns true when pet is deleted`() =
        runTest {
            val repo = FakePetRepository()
            val pet =
                Pet(
                    id = 5L,
                    name = "Rex",
                    species = Species.Dog,
                    birthDate = sampleDate,
                    subcategory = "medium",
                )
            repo.seed(listOf(pet))
            val useCase = DeletePetUseCase(repo)
            assertTrue(useCase(5L))
            assertNull(repo.getById(5L))
        }

    @Test
    fun `returns false when pet does not exist`() =
        runTest {
            val repo = FakePetRepository()
            val useCase = DeletePetUseCase(repo)
            assertFalse(useCase(999L))
        }
}
