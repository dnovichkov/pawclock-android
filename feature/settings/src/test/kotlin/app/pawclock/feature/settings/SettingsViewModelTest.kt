@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package app.pawclock.feature.settings

import app.cash.turbine.test
import app.pawclock.datastore.AppSettings
import app.pawclock.feature.settings.fakes.FakeSettingsRepository
import app.pawclock.model.CalculationMethod
import app.pawclock.model.ThemeMode
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
 * TDD-тесты [SettingsViewModel] (Task 21 / Plan 1).
 *
 * ViewModel — тонкая обёртка над [app.pawclock.datastore.SettingsRepository]:
 *  - читает текущие настройки через `observe()` и публикует как [SettingsState];
 *  - каждое UI-событие триггерит соответствующий suspend setter (fire-and-forget);
 *  - изменения возвращаются через `observe()` Flow → state автоматически обновляется
 *    при следующей эмиссии DataStore.
 *
 * Тесты используют [FakeSettingsRepository] вместо реального DataStore — это позволяет
 * проверять и публичный state (Flow<SettingsState>), и сами вызовы setters'ов.
 *
 * Используется `UnconfinedTestDispatcher` чтобы launch'нутые корутины (вызовы setter'ов
 * в viewModelScope.launch) выполнялись синхронно относительно тест-кода.
 */
class SettingsViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        // viewModelScope использует Dispatchers.Main — для unit-тестов подменяем.
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state reflects current AppSettings from repository (defaults)`() =
        runTest {
            val repository = FakeSettingsRepository()
            val viewModel = SettingsViewModel(repository)

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
            val viewModel = SettingsViewModel(repository)

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
            val viewModel = SettingsViewModel(repository)

            viewModel.handleEvent(SettingsEvent.SetThemeMode(ThemeMode.Dark))

            assertEquals(1, repository.writeCount)
            assertEquals(ThemeMode.Dark, repository.writes.last().themeMode)
        }

    @Test
    fun `SetThemeMode event eventually updates exposed state Flow`() =
        runTest {
            val repository = FakeSettingsRepository()
            val viewModel = SettingsViewModel(repository)

            viewModel.state.test {
                // Изначальное значение.
                assertEquals(ThemeMode.System, awaitItem().themeMode)

                viewModel.handleEvent(SettingsEvent.SetThemeMode(ThemeMode.Light))
                assertEquals(ThemeMode.Light, awaitItem().themeMode)
            }
        }

    @Test
    fun `SetLanguageTag with non-null value persists`() =
        runTest {
            val repository = FakeSettingsRepository()
            val viewModel = SettingsViewModel(repository)

            viewModel.handleEvent(SettingsEvent.SetLanguageTag("en"))

            assertEquals("en", repository.writes.last().languageTag)
        }

    @Test
    fun `SetLanguageTag with null falls back to system locale`() =
        runTest {
            val repository =
                FakeSettingsRepository(
                    initial = AppSettings.Default.copy(languageTag = "ru"),
                )
            val viewModel = SettingsViewModel(repository)

            viewModel.handleEvent(SettingsEvent.SetLanguageTag(null))

            assertNull(repository.writes.last().languageTag)
        }

    @Test
    fun `SetDynamicColor toggles persisted value`() =
        runTest {
            val repository = FakeSettingsRepository()
            val viewModel = SettingsViewModel(repository)

            viewModel.handleEvent(SettingsEvent.SetDynamicColor(enabled = false))

            assertFalse(repository.writes.last().dynamicColor)
            assertEquals(1, repository.writeCount)
        }

    @Test
    fun `SetDefaultCalculationMethod persists Wang↔Size choice`() =
        runTest {
            val repository = FakeSettingsRepository()
            val viewModel = SettingsViewModel(repository)

            viewModel.handleEvent(SettingsEvent.SetDefaultCalculationMethod(CalculationMethod.SIZE_BASED))

            assertEquals(CalculationMethod.SIZE_BASED, repository.writes.last().defaultCalculationMethod)
        }

    @Test
    fun `multiple events update state independently`() =
        runTest {
            val repository = FakeSettingsRepository()
            val viewModel = SettingsViewModel(repository)

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
}
