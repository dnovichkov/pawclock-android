package app.pawclock.domain.usecase

import app.cash.turbine.test
import app.pawclock.domain.fakes.FakePetRepository
import app.pawclock.model.Pet
import app.pawclock.model.Species
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class GetPetsUseCaseTest {
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

    @Test
    fun `emits empty list when no pets`() =
        runTest {
            val repo = FakePetRepository()
            val useCase = GetPetsUseCase(repo)
            useCase().test {
                assertEquals(emptyList<Pet>(), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `emits pets sorted case-insensitively by name`() =
        runTest {
            val repo = FakePetRepository()
            repo.seed(listOf(pet(1, "Чарли"), pet(2, "Альфа"), pet(3, "бобик")))
            val useCase = GetPetsUseCase(repo)

            useCase().test {
                val names = awaitItem().map { it.name }
                assertEquals(listOf("Альфа", "бобик", "Чарли"), names)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `emits new list after insert`() =
        runTest {
            val repo = FakePetRepository()
            val useCase = GetPetsUseCase(repo)

            useCase().test {
                assertEquals(emptyList<Pet>(), awaitItem())
                repo.insert(pet(0, "Rex"))
                val next = awaitItem()
                assertEquals(1, next.size)
                assertEquals("Rex", next.first().name)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
