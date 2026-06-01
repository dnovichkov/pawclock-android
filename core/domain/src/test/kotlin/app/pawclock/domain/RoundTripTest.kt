package app.pawclock.domain

import app.pawclock.domain.export.ExportFormat
import app.pawclock.domain.export.ExportPetsUseCase
import app.pawclock.domain.fakes.FakePetRepository
import app.pawclock.domain.import_.ImportPetsUseCase
import app.pawclock.domain.import_.ImportStrategy
import app.pawclock.model.Gender
import app.pawclock.model.Pet
import app.pawclock.model.Species
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * Финальная приёмка export/import round-trip (Plan 2, Task 24).
 *
 * Создаёт по одному питомцу каждого из 12 видов (с разнообразными полями: подкатегории, пол,
 * вес, заметки с запятыми/кавычками/переводами строк), затем для каждого формата прогоняет
 * полный цикл **export → clearAll → import** и проверяет, что восстановленное содержимое
 * совпадает с исходным по всем переносимым полям.
 *
 * `id` и `photoPath` намеренно не экспортируются (локальные artifact-поля, см. [app.pawclock.domain.export.PetExportEntry]),
 * поэтому сравнение нормализует их к `0L`/`null`. Тест — единая точка, доказывающая контракт §3.5:
 * «бэкап сохраняет всё, кроме локальных артефактов» — для всех видов и обоих форматов сразу.
 */
class RoundTripTest {
    private val fixedClock: Clock = Clock.fixed(Instant.parse("2026-06-01T10:00:00Z"), ZoneOffset.UTC)

    /** По одному питомцу каждого из 12 видов; id=0L и photoPath=null (исходное состояние). */
    private fun twelvePets(): List<Pet> =
        listOf(
            Pet(
                0L,
                "Рекс",
                Species.Dog,
                LocalDate.of(2019, 3, 14),
                "medium",
                Gender.Male,
                18.5,
                "Любит играть с мячом",
            ),
            Pet(
                0L,
                "Мурка",
                Species.Cat,
                LocalDate.of(2020, 6, 1),
                "indoor_short_hair",
                Gender.Female,
                4.25,
                "Спит, много; «диван»",
            ),
            Pet(0L, "Снежок", Species.Rabbit, LocalDate.of(2022, 1, 5), "dwarf", null, 1.5, null),
            Pet(
                0L,
                "Хома",
                Species.Hamster,
                LocalDate.of(2025, 2, 2),
                "syrian",
                Gender.Male,
                0.125,
                "крутит колесо",
            ),
            Pet(0L, "Пушок", Species.GuineaPig, LocalDate.of(2023, 9, 9), null, Gender.Female, 1.0, null),
            Pet(
                0L,
                "Шуша",
                Species.Rat,
                LocalDate.of(2025, 5, 20),
                null,
                Gender.Unknown,
                0.5,
                "заметка с \"кавычками\"",
            ),
            Pet(0L, "Пик", Species.Mouse, LocalDate.of(2026, 1, 10), null, null, 0.03, "много\nстрок"),
            Pet(0L, "Локи", Species.Ferret, LocalDate.of(2021, 7, 7), null, Gender.Male, 1.2, null),
            Pet(
                0L,
                "Кеша",
                Species.Bird,
                LocalDate.of(2024, 4, 1),
                "budgerigar",
                Gender.Male,
                0.035,
                "говорит «привет»",
            ),
            Pet(
                0L,
                "Годзилла",
                Species.Reptile,
                LocalDate.of(2023, 11, 11),
                "bearded_dragon",
                Gender.Female,
                0.4,
                null,
            ),
            Pet(
                0L,
                "Буцефал",
                Species.Horse,
                LocalDate.of(2015, 5, 5),
                "light_horse",
                Gender.Male,
                450.0,
                "быстрый, «чемпион»",
            ),
            Pet(0L, "Немо", Species.Fish, LocalDate.of(2025, 8, 18), "goldfish", Gender.Unknown, null, null),
        )

    /** Стрипает локальные artifact-поля, которые не переживают экспорт by design. */
    private fun Pet.normalized(): Pet = copy(id = 0L, photoPath = null)

    private suspend fun assertRoundTripPreservesAllPets(format: ExportFormat) {
        val repo = FakePetRepository()
        val exportUseCase = ExportPetsUseCase(repo, fixedClock)
        val importUseCase = ImportPetsUseCase(repo, fixedClock)

        val original = twelvePets()
        original.forEach { repo.insert(it) }

        val serialized = exportUseCase(format)
        repo.clearAll()
        assertTrue(repo.getAll().isEmpty(), "$format: clearAll должен опустошить репозиторий")

        val summary = importUseCase(serialized, ImportStrategy.MERGE, format = format)
        assertEquals(12, summary.importedCount, "$format: должны импортироваться все 12 питомцев")

        val restored = repo.getAll().map { it.normalized() }
        val expected = original.map { it.normalized() }.sortedBy { it.name.lowercase() }

        assertEquals(expected.size, restored.size, "$format: число питомцев изменилось")
        assertEquals(expected, restored, "$format: содержимое после round-trip не совпало с исходным")
        assertTrue(
            restored.map { it.species }.toSet() == Species.all().toSet(),
            "$format: после round-trip присутствуют не все 12 видов",
        )
    }

    @Test
    fun `JSON export-import round-trip preserves all twelve species`() =
        runTest {
            assertRoundTripPreservesAllPets(ExportFormat.JSON)
        }

    @Test
    fun `CSV export-import round-trip preserves all twelve species`() =
        runTest {
            assertRoundTripPreservesAllPets(ExportFormat.CSV)
        }
}
