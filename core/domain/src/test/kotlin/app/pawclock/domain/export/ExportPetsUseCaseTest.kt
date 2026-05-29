package app.pawclock.domain.export

import app.pawclock.domain.fakes.FakePetRepository
import app.pawclock.domain.pet.PetRepository
import app.pawclock.model.Pet
import app.pawclock.model.Species
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json

/**
 * Тесты для [ExportPetsUseCase] (Plan 2, Task 17).
 *
 * UseCase читает всех питомцев через [PetRepository.getAll], проставляет момент экспорта
 * из инжектированного [Clock] и делегирует сериализацию формат-специфичному сериализатору.
 * CSV-ветка добавляется в Task 18 — пока проверяется, что она бросает NotImplementedError.
 */
class ExportPetsUseCaseTest {
    private val exportedAt: Instant = Instant.parse("2026-05-29T12:00:00Z")
    private val clock: Clock = Clock.fixed(exportedAt, ZoneOffset.UTC)

    private fun pet(
        name: String,
        species: Species = Species.Dog,
        subcategory: String? = "medium",
    ): Pet =
        Pet(
            id = 0L,
            name = name,
            species = species,
            birthDate = LocalDate.of(2021, 1, 1),
            subcategory = subcategory,
        )

    @Test
    fun `export empty list yields JSON with empty pets array`() =
        runTest {
            val useCase = ExportPetsUseCase(FakePetRepository(), clock)

            val output = useCase(ExportFormat.JSON)

            val schema = Json.decodeFromString(PetsExportSchema.serializer(), output)
            assertTrue(schema.pets.isEmpty())
            assertEquals(exportedAt, Instant.parse(schema.exportedAt))
        }

    @Test
    fun `export multiple pets matches repository content`() =
        runTest {
            val repo = FakePetRepository()
            repo.seed(listOf(pet("Рекс"), pet("Мурка", Species.Cat, "indoor_short_hair")))
            val useCase = ExportPetsUseCase(repo, clock)

            val output = useCase(ExportFormat.JSON)

            val schema = Json.decodeFromString(PetsExportSchema.serializer(), output)
            assertEquals(2, schema.pets.size)
            assertEquals(setOf("Мурка", "Рекс"), schema.pets.map { it.name }.toSet())
        }

    @Test
    fun `uses clock instant as exportedAt`() =
        runTest {
            val useCase = ExportPetsUseCase(FakePetRepository(), clock)

            val output = useCase(ExportFormat.JSON)

            val schema = Json.decodeFromString(PetsExportSchema.serializer(), output)
            assertEquals("2026-05-29T12:00:00Z", schema.exportedAt)
        }

    @Test
    fun `propagates exception from repository`() =
        runTest {
            val useCase = ExportPetsUseCase(ThrowingPetRepository(), clock)

            assertFailsWith<IOException> { useCase(ExportFormat.JSON) }
        }

    @Test
    fun `csv export is not yet implemented`() =
        runTest {
            val useCase = ExportPetsUseCase(FakePetRepository(), clock)

            // CSV-сериализатор появляется в Task 18; до тех пор ветка явно не реализована.
            assertFailsWith<NotImplementedError> { useCase(ExportFormat.CSV) }
        }

    /** [PetRepository], у которого чтение всегда падает — для проверки проброса IO-ошибок. */
    private class ThrowingPetRepository : PetRepository {
        override fun observeAll(): Flow<List<Pet>> = emptyFlow()

        override suspend fun getById(id: Long): Pet? = null

        override suspend fun insert(pet: Pet): Long = error("not used")

        override suspend fun update(pet: Pet) = error("not used")

        override suspend fun deleteById(id: Long): Int = 0

        override suspend fun getAll(): List<Pet> = throw IOException("disk read failed")
    }
}
