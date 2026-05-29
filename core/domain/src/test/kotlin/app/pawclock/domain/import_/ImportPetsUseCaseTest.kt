package app.pawclock.domain.import_

import app.pawclock.domain.export.ExportFormat
import app.pawclock.domain.fakes.FakePetRepository
import app.pawclock.model.Pet
import app.pawclock.model.Species
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * Тесты для [ImportPetsUseCase] (Plan 2, Task 19).
 *
 * UseCase декодирует ввод через [PetsJsonDeserializer], конвертирует [PetsImportResult.Success]
 * в доменные [Pet] (id=0L — Room назначит новый) и применяет [ImportStrategy]:
 *  - [ImportStrategy.MERGE] — добавляет к существующим;
 *  - [ImportStrategy.REPLACE] — очищает репозиторий перед вставкой.
 *
 * `dryRun = true` возвращает предпросмотр (count + warnings) без мутации репозитория —
 * для confirm-диалога в Settings UI (Task 21). На [PetsImportResult.Failure] UseCase
 * пробрасывает [ImportException].
 */
class ImportPetsUseCaseTest {
    private fun jsonWith(vararg petBlocks: String): String =
        """
        {"schema_version":1,"exported_at":"2026-05-29T12:00:00Z","pets":[${petBlocks.joinToString(",")}]}
        """.trimIndent()

    private fun petBlock(
        name: String,
        speciesId: String = "dog",
        birthDate: String = "2020-01-01",
    ): String = """{"name":"$name","species_id":"$speciesId","birth_date":"$birthDate"}"""

    private fun existing(
        name: String,
        id: Long = 0L,
    ): Pet =
        Pet(
            id = id,
            name = name,
            species = Species.Cat,
            birthDate = LocalDate.of(2019, 1, 1),
            subcategory = "indoor_short_hair",
        )

    @Test
    fun `imports valid pets and inserts into repository`() =
        runTest {
            val repo = FakePetRepository()
            val useCase = ImportPetsUseCase(repo)

            val summary = useCase(jsonWith(petBlock("Рекс"), petBlock("Бобик")), ImportStrategy.MERGE)

            assertEquals(2, summary.importedCount)
            val stored = repo.getAll()
            assertEquals(setOf("Бобик", "Рекс"), stored.map { it.name }.toSet())
            assertTrue(stored.all { it.id != 0L }, "вставленным питомцам Room назначает id ≥ 1")
        }

    @Test
    fun `dry-run returns preview without insertion`() =
        runTest {
            val repo = FakePetRepository()
            val useCase = ImportPetsUseCase(repo)

            val summary = useCase(jsonWith(petBlock("Рекс")), ImportStrategy.MERGE, dryRun = true)

            assertEquals(1, summary.importedCount)
            assertTrue(summary.dryRun)
            assertTrue(repo.getAll().isEmpty(), "dry-run не должен ничего вставлять")
        }

    @Test
    fun `replace strategy clears existing pets before insert`() =
        runTest {
            val repo = FakePetRepository()
            repo.seed(listOf(existing("Мурка", id = 1L), existing("Барсик", id = 2L)))
            val useCase = ImportPetsUseCase(repo)

            useCase(jsonWith(petBlock("Рекс")), ImportStrategy.REPLACE)

            val stored = repo.getAll()
            assertEquals(listOf("Рекс"), stored.map { it.name })
        }

    @Test
    fun `merge strategy keeps existing pets and inserts new`() =
        runTest {
            val repo = FakePetRepository()
            repo.seed(listOf(existing("Мурка", id = 1L)))
            val useCase = ImportPetsUseCase(repo)

            useCase(jsonWith(petBlock("Рекс")), ImportStrategy.MERGE)

            assertEquals(setOf("Мурка", "Рекс"), repo.getAll().map { it.name }.toSet())
        }

    @Test
    fun `replace dry-run does not clear existing pets`() =
        runTest {
            val repo = FakePetRepository()
            repo.seed(listOf(existing("Мурка", id = 1L)))
            val useCase = ImportPetsUseCase(repo)

            val summary = useCase(jsonWith(petBlock("Рекс")), ImportStrategy.REPLACE, dryRun = true)

            assertEquals(1, summary.importedCount)
            assertEquals(listOf("Мурка"), repo.getAll().map { it.name }, "dry-run REPLACE не очищает")
        }

    @Test
    fun `propagates ImportException for malformed input`() =
        runTest {
            val useCase = ImportPetsUseCase(FakePetRepository())

            assertFailsWith<ImportException> { useCase("{ not json", ImportStrategy.MERGE) }
        }

    @Test
    fun `propagates ImportException for unknown species`() =
        runTest {
            val repo = FakePetRepository()
            val useCase = ImportPetsUseCase(repo)

            assertFailsWith<ImportException.UnknownSpecies> {
                useCase(jsonWith(petBlock("Дракоша", speciesId = "dragon")), ImportStrategy.MERGE)
            }
            assertTrue(repo.getAll().isEmpty(), "при ошибке ничего не вставлено")
        }

    @Test
    fun `maps entry fields into domain pet`() =
        runTest {
            val repo = FakePetRepository()
            val useCase = ImportPetsUseCase(repo)
            val json =
                """
                {"schema_version":1,"exported_at":"x","pets":[
                  {"name":"Рекс","species_id":"dog","subcategory_id":"large",
                   "birth_date":"2020-03-15","gender_id":"male","weight_kg":30.0,"notes":"добрый"}]}
                """.trimIndent()

            useCase(json, ImportStrategy.MERGE)

            val pet = repo.getAll().single()
            assertEquals("Рекс", pet.name)
            assertEquals(Species.Dog, pet.species)
            assertEquals("large", pet.subcategory)
            assertEquals(LocalDate.of(2020, 3, 15), pet.birthDate)
            assertEquals(app.pawclock.model.Gender.Male, pet.gender)
            assertEquals(30.0, pet.weightKg)
            assertEquals("добрый", pet.notes)
        }

    @Test
    fun `unknown gender is dropped to null and surfaced as warning`() =
        runTest {
            val repo = FakePetRepository()
            val useCase = ImportPetsUseCase(repo)
            val json =
                """
                {"schema_version":1,"exported_at":"x","pets":[
                  {"name":"Рекс","species_id":"dog","birth_date":"2020-01-01","gender_id":"neuter"}]}
                """.trimIndent()

            val summary = useCase(json, ImportStrategy.MERGE)

            assertEquals(1, summary.warnings.size)
            assertEquals(null, repo.getAll().single().gender)
        }

    private val csvHeader = "name,species_id,subcategory_id,birth_date,gender_id,weight_kg,notes"

    private fun csvWith(vararg rows: String): String = (listOf(csvHeader) + rows).joinToString("\r\n")

    @Test
    fun `auto-detects CSV input and imports pets`() =
        runTest {
            val repo = FakePetRepository()
            val useCase = ImportPetsUseCase(repo)

            val summary =
                useCase(
                    csvWith("Рекс,dog,,2020-01-01,,,", "Бобик,dog,,2021-02-02,,,"),
                    ImportStrategy.MERGE,
                )

            assertEquals(2, summary.importedCount)
            assertEquals(setOf("Бобик", "Рекс"), repo.getAll().map { it.name }.toSet())
        }

    @Test
    fun `auto-detects JSON input even when format not specified`() =
        runTest {
            val repo = FakePetRepository()
            val useCase = ImportPetsUseCase(repo)

            useCase(jsonWith(petBlock("Рекс")), ImportStrategy.MERGE)

            assertEquals(listOf("Рекс"), repo.getAll().map { it.name })
        }

    @Test
    fun `explicit CSV format maps entry fields into domain pet`() =
        runTest {
            val repo = FakePetRepository()
            val useCase = ImportPetsUseCase(repo)

            useCase(
                csvWith("Рекс,dog,large,2020-03-15,male,30.0,добрый"),
                ImportStrategy.MERGE,
                format = ExportFormat.CSV,
            )

            val pet = repo.getAll().single()
            assertEquals("Рекс", pet.name)
            assertEquals(Species.Dog, pet.species)
            assertEquals("large", pet.subcategory)
            assertEquals(LocalDate.of(2020, 3, 15), pet.birthDate)
            assertEquals(app.pawclock.model.Gender.Male, pet.gender)
            assertEquals(30.0, pet.weightKg)
            assertEquals("добрый", pet.notes)
        }

    @Test
    fun `CSV replace strategy clears existing pets before insert`() =
        runTest {
            val repo = FakePetRepository()
            repo.seed(listOf(existing("Мурка", id = 1L)))
            val useCase = ImportPetsUseCase(repo)

            useCase(csvWith("Рекс,dog,,2020-01-01,,,"), ImportStrategy.REPLACE)

            assertEquals(listOf("Рекс"), repo.getAll().map { it.name })
        }

    @Test
    fun `propagates ImportException for malformed CSV (unknown species)`() =
        runTest {
            val repo = FakePetRepository()
            val useCase = ImportPetsUseCase(repo)

            assertFailsWith<ImportException.UnknownSpecies> {
                useCase(csvWith("Дракоша,dragon,,2020-01-01,,,"), ImportStrategy.MERGE)
            }
            assertTrue(repo.getAll().isEmpty(), "при ошибке ничего не вставлено")
        }
}
