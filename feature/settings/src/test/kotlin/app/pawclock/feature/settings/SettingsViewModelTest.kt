@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package app.pawclock.feature.settings

import app.cash.turbine.test
import app.pawclock.datastore.AppSettings
import app.pawclock.datastore.SettingsRepository
import app.pawclock.domain.export.ExportFormat
import app.pawclock.domain.export.ExportPetsUseCase
import app.pawclock.domain.import_.ImportPetsUseCase
import app.pawclock.domain.import_.ImportStrategy
import app.pawclock.domain.import_.ImportWarning
import app.pawclock.domain.locale.LocaleApplier
import app.pawclock.domain.pet.PetRepository
import app.pawclock.feature.settings.backup.BackupFileReader
import app.pawclock.feature.settings.backup.BackupFileWriter
import app.pawclock.feature.settings.fakes.FakeBackupFileReader
import app.pawclock.feature.settings.fakes.FakeBackupFileWriter
import app.pawclock.feature.settings.fakes.FakeLocaleApplier
import app.pawclock.feature.settings.fakes.FakePetRepository
import app.pawclock.feature.settings.fakes.FakeSettingsRepository
import app.pawclock.model.CalculationMethod
import app.pawclock.model.Pet
import app.pawclock.model.Species
import app.pawclock.model.ThemeMode
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * TDD-тесты [SettingsViewModel] (Task 21 / Plan 1 + Plan 2 Task 21).
 *
 * Plan 1: ViewModel — тонкая обёртка над [SettingsRepository] (read-through proxy для настроек).
 * Plan 2 (§3.5): экспорт/импорт через one-time [SettingsViewModel.effects] (Channel→Flow) и
 * SAF-порты [BackupFileWriter]/[BackupFileReader].
 *
 * Используется `UnconfinedTestDispatcher` чтобы launch'нутые корутины (вызовы setter'ов /
 * export-import) выполнялись синхронно относительно тест-кода.
 */
class SettingsViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val fixedClock = Clock.fixed(Instant.parse("2026-05-29T00:00:00Z"), ZoneOffset.UTC)

    @BeforeEach
    fun setUp() {
        // viewModelScope использует Dispatchers.Main — для unit-тестов подменяем.
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Фабрика ViewModel'и с дефолтными fake-зависимостями. Тесты переопределяют только то,
     * что им нужно (например, [petRepository] для export-count или [backupFileReader] для импорта).
     */
    private fun buildViewModel(
        repository: SettingsRepository = FakeSettingsRepository(),
        localeApplier: LocaleApplier = FakeLocaleApplier(),
        petRepository: PetRepository = FakePetRepository(),
        backupFileWriter: BackupFileWriter = FakeBackupFileWriter(),
        backupFileReader: BackupFileReader = FakeBackupFileReader(),
    ): SettingsViewModel =
        SettingsViewModel(
            repository = repository,
            localeApplier = localeApplier,
            exportPets = ExportPetsUseCase(petRepository, fixedClock),
            importPets = ImportPetsUseCase(petRepository, fixedClock),
            backupFileWriter = backupFileWriter,
            backupFileReader = backupFileReader,
        )

    private fun pet(
        name: String,
        species: Species = Species.Dog,
    ): Pet = Pet(id = 0L, name = name, species = species, birthDate = LocalDate.parse("2020-06-15"))

    // ---------------------------------------------------------------------------------------------
    // Plan 1 — настройки.
    // ---------------------------------------------------------------------------------------------

    @Test
    fun `initial state reflects current AppSettings from repository (defaults)`() =
        runTest {
            val viewModel = buildViewModel()

            viewModel.state.test {
                val state = awaitItem()
                assertEquals(ThemeMode.System, state.themeMode)
                assertNull(state.languageTag)
                assertTrue(state.dynamicColor, "Default dynamicColor must be true (opt-in)")
                assertEquals(CalculationMethod.EPIGENETIC, state.defaultCalculationMethod)
            }
        }

    @Test
    fun `initial state reflects custom repository state (Dark theme + Russian + SizeBased)`() =
        runTest {
            val repository =
                FakeSettingsRepository(
                    initial =
                        AppSettings(
                            themeMode = ThemeMode.Dark,
                            languageTag = "ru",
                            dynamicColor = false,
                            defaultCalculationMethod = CalculationMethod.SIZE_BASED,
                        ),
                )
            val viewModel = buildViewModel(repository = repository)

            viewModel.state.test {
                val state = awaitItem()
                assertEquals(ThemeMode.Dark, state.themeMode)
                assertEquals("ru", state.languageTag)
                assertFalse(state.dynamicColor, "Custom state has dynamicColor disabled")
                assertEquals(CalculationMethod.SIZE_BASED, state.defaultCalculationMethod)
            }
        }

    @Test
    fun `SetThemeMode event persists new value through repository`() =
        runTest {
            val repository = FakeSettingsRepository()
            val viewModel = buildViewModel(repository = repository)

            viewModel.handleEvent(SettingsEvent.SetThemeMode(ThemeMode.Dark))

            assertEquals(1, repository.writeCount)
            assertEquals(ThemeMode.Dark, repository.writes.last().themeMode)
        }

    @Test
    fun `SetThemeMode event eventually updates exposed state Flow`() =
        runTest {
            val viewModel = buildViewModel()

            viewModel.state.test {
                // Изначальное значение.
                assertEquals(ThemeMode.System, awaitItem().themeMode)

                viewModel.handleEvent(SettingsEvent.SetThemeMode(ThemeMode.Light))
                assertEquals(ThemeMode.Light, awaitItem().themeMode)
            }
        }

    @Test
    fun `SetLanguageTag with non-null value persists and applies locale`() =
        runTest {
            val repository = FakeSettingsRepository()
            val applier = FakeLocaleApplier()
            val viewModel = buildViewModel(repository = repository, localeApplier = applier)

            viewModel.handleEvent(SettingsEvent.SetLanguageTag("en"))

            assertEquals("en", repository.writes.last().languageTag)
            assertEquals(1, applier.applyCount, "LocaleApplier must be invoked once per SetLanguageTag")
            assertEquals("en", applier.lastTag)
        }

    @Test
    fun `SetLanguageTag with null falls back to system locale and applies null`() =
        runTest {
            val repository =
                FakeSettingsRepository(
                    initial = AppSettings.Default.copy(languageTag = "ru"),
                )
            val applier = FakeLocaleApplier()
            val viewModel = buildViewModel(repository = repository, localeApplier = applier)

            viewModel.handleEvent(SettingsEvent.SetLanguageTag(null))

            assertNull(repository.writes.last().languageTag)
            assertNull(applier.lastTag, "Null tag must propagate to applier to clear app locale")
        }

    @Test
    fun `events other than SetLanguageTag do not invoke LocaleApplier`() =
        runTest {
            val applier = FakeLocaleApplier()
            val viewModel = buildViewModel(localeApplier = applier)

            viewModel.handleEvent(SettingsEvent.SetThemeMode(ThemeMode.Dark))
            viewModel.handleEvent(SettingsEvent.SetDynamicColor(enabled = false))
            viewModel.handleEvent(SettingsEvent.SetDefaultCalculationMethod(CalculationMethod.SIZE_BASED))

            assertEquals(0, applier.applyCount, "LocaleApplier must only react to SetLanguageTag events")
        }

    @Test
    fun `SetDynamicColor toggles persisted value`() =
        runTest {
            val repository = FakeSettingsRepository()
            val viewModel = buildViewModel(repository = repository)

            viewModel.handleEvent(SettingsEvent.SetDynamicColor(enabled = false))

            assertFalse(repository.writes.last().dynamicColor)
            assertEquals(1, repository.writeCount)
        }

    @Test
    fun `SetDefaultCalculationMethod persists Wang↔Size choice`() =
        runTest {
            val repository = FakeSettingsRepository()
            val viewModel = buildViewModel(repository = repository)

            viewModel.handleEvent(SettingsEvent.SetDefaultCalculationMethod(CalculationMethod.SIZE_BASED))

            assertEquals(CalculationMethod.SIZE_BASED, repository.writes.last().defaultCalculationMethod)
        }

    @Test
    fun `multiple events update state independently`() =
        runTest {
            val viewModel = buildViewModel()

            viewModel.state.test {
                // Default-снапшот.
                val initial = awaitItem()
                assertEquals(ThemeMode.System, initial.themeMode)
                assertTrue(initial.dynamicColor)

                viewModel.handleEvent(SettingsEvent.SetThemeMode(ThemeMode.Dark))
                val afterTheme = awaitItem()
                assertEquals(ThemeMode.Dark, afterTheme.themeMode)
                assertTrue(afterTheme.dynamicColor, "DynamicColor must remain unchanged after SetThemeMode")

                viewModel.handleEvent(SettingsEvent.SetDynamicColor(enabled = false))
                val afterDynamic = awaitItem()
                assertEquals(
                    ThemeMode.Dark,
                    afterDynamic.themeMode,
                    "Theme must remain Dark after SetDynamicColor",
                )
                assertFalse(afterDynamic.dynamicColor)
            }
        }

    // ---------------------------------------------------------------------------------------------
    // Plan 2 (§3.5) — экспорт.
    // ---------------------------------------------------------------------------------------------

    @Test
    fun `ExportRequested JSON emits RequestSaveLocation with json filename and mime`() =
        runTest {
            val viewModel = buildViewModel()

            viewModel.effects.test {
                viewModel.handleEvent(SettingsEvent.ExportRequested(ExportFormat.JSON))

                val effect = awaitItem()
                assertIs<SettingsEffect.RequestSaveLocation>(effect)
                assertEquals("application/json", effect.mimeType)
                assertTrue(
                    effect.suggestedFilename.endsWith(".json"),
                    "JSON export filename must end with .json, was ${effect.suggestedFilename}",
                )
            }
        }

    @Test
    fun `ExportRequested CSV emits RequestSaveLocation with csv filename and mime`() =
        runTest {
            val viewModel = buildViewModel()

            viewModel.effects.test {
                viewModel.handleEvent(SettingsEvent.ExportRequested(ExportFormat.CSV))

                val effect = awaitItem()
                assertIs<SettingsEffect.RequestSaveLocation>(effect)
                assertEquals("text/csv", effect.mimeType)
                assertTrue(
                    effect.suggestedFilename.endsWith(".csv"),
                    "CSV export filename must end with .csv, was ${effect.suggestedFilename}",
                )
            }
        }

    @Test
    fun `ExportLocationSelected writes file and emits ExportComplete with pet count`() =
        runTest {
            val petRepository = FakePetRepository(initial = listOf(pet("Рекс"), pet("Мурка", Species.Cat)))
            val writer = FakeBackupFileWriter()
            val viewModel = buildViewModel(petRepository = petRepository, backupFileWriter = writer)

            viewModel.effects.test {
                viewModel.handleEvent(SettingsEvent.ExportRequested(ExportFormat.JSON))
                assertIs<SettingsEffect.RequestSaveLocation>(awaitItem())

                viewModel.handleEvent(SettingsEvent.ExportLocationSelected("content://backup.json"))
                val complete = awaitItem()
                assertIs<SettingsEffect.ExportComplete>(complete)
                assertEquals(2, complete.petCount)
            }

            assertEquals(1, writer.writeCount, "Export must write exactly once")
            assertEquals("content://backup.json", writer.lastUri)
            assertTrue(
                writer.lastContent?.contains("\"species_id\": \"dog\"") == true,
                "Written JSON must contain serialized pet content",
            )
        }

    @Test
    fun `ExportLocationSelected emits ExportError when writer fails`() =
        runTest {
            val petRepository = FakePetRepository(initial = listOf(pet("Рекс")))
            val writer = FakeBackupFileWriter(failWith = IOException("disk full"))
            val viewModel = buildViewModel(petRepository = petRepository, backupFileWriter = writer)

            viewModel.effects.test {
                viewModel.handleEvent(SettingsEvent.ExportRequested(ExportFormat.JSON))
                assertIs<SettingsEffect.RequestSaveLocation>(awaitItem())

                viewModel.handleEvent(SettingsEvent.ExportLocationSelected("content://backup.json"))
                val error = awaitItem()
                assertIs<SettingsEffect.ExportError>(error)
                assertEquals(SettingsMessages.EXPORT_ERROR_WRITE_FAILED, error.messageKey)
            }
        }

    // ---------------------------------------------------------------------------------------------
    // Plan 2 (§3.5) — импорт.
    // ---------------------------------------------------------------------------------------------

    @Test
    fun `ImportRequested emits RequestOpenLocation with json and csv mimes`() =
        runTest {
            val viewModel = buildViewModel()

            viewModel.effects.test {
                viewModel.handleEvent(SettingsEvent.ImportRequested)

                val effect = awaitItem()
                assertIs<SettingsEffect.RequestOpenLocation>(effect)
                assertEquals(listOf("application/json", "text/csv"), effect.mimeTypes)
            }
        }

    @Test
    fun `ImportLocationSelected imports pets and emits ImportComplete with count`() =
        runTest {
            val petRepository = FakePetRepository()
            val json =
                """
                {"schema_version":1,"exported_at":"2026-05-29T00:00:00Z","pets":[
                  {"name":"Рекс","species_id":"dog","birth_date":"2020-06-15"},
                  {"name":"Мурка","species_id":"cat","birth_date":"2021-01-01"}
                ]}
                """.trimIndent()
            val reader = FakeBackupFileReader(content = json)
            val viewModel = buildViewModel(petRepository = petRepository, backupFileReader = reader)

            viewModel.effects.test {
                viewModel.handleEvent(
                    SettingsEvent.ImportLocationSelected("content://in.json", ImportStrategy.MERGE),
                )
                val complete = awaitItem()
                assertIs<SettingsEffect.ImportComplete>(complete)
                assertEquals(2, complete.petCount)
                assertTrue(complete.warnings.isEmpty())
            }

            assertEquals(2, petRepository.getAll().size, "MERGE must insert imported pets")
        }

    @Test
    fun `ImportLocationSelected with REPLACE clears existing then inserts`() =
        runTest {
            val petRepository = FakePetRepository(initial = listOf(pet("Старый")))
            val json =
                """
                {"schema_version":1,"exported_at":"2026-05-29T00:00:00Z","pets":[
                  {"name":"Новый","species_id":"dog","birth_date":"2020-06-15"}
                ]}
                """.trimIndent()
            val reader = FakeBackupFileReader(content = json)
            val viewModel = buildViewModel(petRepository = petRepository, backupFileReader = reader)

            viewModel.effects.test {
                viewModel.handleEvent(
                    SettingsEvent.ImportLocationSelected("content://in.json", ImportStrategy.REPLACE),
                )
                assertIs<SettingsEffect.ImportComplete>(awaitItem())
            }

            val names = petRepository.getAll().map { it.name }
            assertEquals(listOf("Новый"), names, "REPLACE must drop pre-existing pets")
        }

    @Test
    fun `ImportLocationSelected with unknown species emits ImportError`() =
        runTest {
            val json =
                """
                {"schema_version":1,"exported_at":"2026-05-29T00:00:00Z","pets":[
                  {"name":"Дракоша","species_id":"dragon","birth_date":"2020-06-15"}
                ]}
                """.trimIndent()
            val reader = FakeBackupFileReader(content = json)
            val viewModel = buildViewModel(backupFileReader = reader)

            viewModel.effects.test {
                viewModel.handleEvent(
                    SettingsEvent.ImportLocationSelected("content://in.json", ImportStrategy.MERGE),
                )
                val error = awaitItem()
                assertIs<SettingsEffect.ImportError>(error)
                assertEquals(SettingsMessages.IMPORT_ERROR_UNKNOWN_SPECIES, error.messageKey)
            }
        }

    @Test
    fun `ImportLocationSelected with malformed content emits ImportError malformed`() =
        runTest {
            // Начинается с `{` → auto-detect выбирает JSON, но документ битый → MalformedData.
            val reader = FakeBackupFileReader(content = "{ this is not valid json {{{")
            val viewModel = buildViewModel(backupFileReader = reader)

            viewModel.effects.test {
                viewModel.handleEvent(
                    SettingsEvent.ImportLocationSelected("content://in.json", ImportStrategy.MERGE),
                )
                val error = awaitItem()
                assertIs<SettingsEffect.ImportError>(error)
                assertEquals(SettingsMessages.IMPORT_ERROR_MALFORMED, error.messageKey)
            }
        }

    @Test
    fun `ImportLocationSelected with missing required field emits ImportError missing field`() =
        runTest {
            // Отсутствует обязательное `species_id` → MissingRequiredField → ключ IMPORT_ERROR_MISSING_FIELD.
            val json =
                """
                {"schema_version":1,"exported_at":"2026-05-29T00:00:00Z","pets":[
                  {"name":"Рекс","birth_date":"2020-06-15"}
                ]}
                """.trimIndent()
            val reader = FakeBackupFileReader(content = json)
            val viewModel = buildViewModel(backupFileReader = reader)

            viewModel.effects.test {
                viewModel.handleEvent(
                    SettingsEvent.ImportLocationSelected("content://in.json", ImportStrategy.MERGE),
                )
                val error = awaitItem()
                assertIs<SettingsEffect.ImportError>(error)
                assertEquals(SettingsMessages.IMPORT_ERROR_MISSING_FIELD, error.messageKey)
            }
        }

    @Test
    fun `ImportLocationSelected with forward-incompatible schema version emits ImportError unsupported version`() =
        runTest {
            // schema_version больше текущей поддерживаемой → UnsupportedSchemaVersion.
            val json =
                """
                {"schema_version":2,"exported_at":"2026-05-29T00:00:00Z","pets":[]}
                """.trimIndent()
            val reader = FakeBackupFileReader(content = json)
            val viewModel = buildViewModel(backupFileReader = reader)

            viewModel.effects.test {
                viewModel.handleEvent(
                    SettingsEvent.ImportLocationSelected("content://in.json", ImportStrategy.MERGE),
                )
                val error = awaitItem()
                assertIs<SettingsEffect.ImportError>(error)
                assertEquals(SettingsMessages.IMPORT_ERROR_UNSUPPORTED_VERSION, error.messageKey)
            }
        }

    @Test
    fun `ImportLocationSelected with unknown gender emits ImportComplete with warning`() =
        runTest {
            val petRepository = FakePetRepository()
            val json =
                """
                {"schema_version":1,"exported_at":"2026-05-29T00:00:00Z","pets":[
                  {"name":"Рекс","species_id":"dog","birth_date":"2020-06-15","gender_id":"alien"}
                ]}
                """.trimIndent()
            val reader = FakeBackupFileReader(content = json)
            val viewModel = buildViewModel(petRepository = petRepository, backupFileReader = reader)

            viewModel.effects.test {
                viewModel.handleEvent(
                    SettingsEvent.ImportLocationSelected("content://in.json", ImportStrategy.MERGE),
                )
                val complete = awaitItem()
                assertIs<SettingsEffect.ImportComplete>(complete)
                assertEquals(1, complete.petCount)
                assertEquals(1, complete.warnings.size)
                assertIs<ImportWarning.UnknownGender>(complete.warnings.first())
            }
        }

    @Test
    fun `ImportLocationSelected emits ImportError when reader fails`() =
        runTest {
            val reader = FakeBackupFileReader(failWith = IOException("permission revoked"))
            val viewModel = buildViewModel(backupFileReader = reader)

            viewModel.effects.test {
                viewModel.handleEvent(
                    SettingsEvent.ImportLocationSelected("content://in.json", ImportStrategy.MERGE),
                )
                val error = awaitItem()
                assertIs<SettingsEffect.ImportError>(error)
                assertEquals(SettingsMessages.IMPORT_ERROR_READ_FAILED, error.messageKey)
            }
        }
}
