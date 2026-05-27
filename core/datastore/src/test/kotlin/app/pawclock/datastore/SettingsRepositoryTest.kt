@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package app.pawclock.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import app.cash.turbine.test
import app.pawclock.model.CalculationMethod
import app.pawclock.model.ThemeMode
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * TDD-тесты [SettingsRepository] (см. Task 13 в плане pawclock-foundation-and-dog-cat-mvp.md).
 *
 * Тесты выполняются на чистом JVM — `PreferenceDataStoreFactory.create()` не требует Context.
 * Каждый тест получает свежую DataStore-файл через JUnit 5 `@TempDir`, чтобы изоляция
 * была гарантирована (DataStore кеширует instance по pathname — если переиспользовать
 * файл, в следующем тесте получим состояние из предыдущего).
 *
 * Используется `UnconfinedTestDispatcher` потому что DataStore операции должны быть
 * детерминированно сериализованы относительно `runTest` body — иначе `repository.observe()
 * .first()` может вернуть default'ы до того, как write завершится.
 */
class SettingsRepositoryTest {
    @TempDir
    lateinit var tempDir: File

    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: SettingsRepository

    @BeforeEach
    fun setUp() {
        dataStoreScope = CoroutineScope(UnconfinedTestDispatcher() + SupervisorJob())
        dataStore =
            PreferenceDataStoreFactory.create(
                scope = dataStoreScope,
                produceFile = { File(tempDir, "settings_test.preferences_pb") },
            )
        repository = SettingsRepositoryImpl(dataStore)
    }

    @AfterEach
    fun tearDown() {
        // Закрываем coroutine-scope чтобы не было ресурс-лика между тестами.
        dataStoreScope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
    }

    @Test
    fun `default theme mode is System when no value was ever written`() =
        runTest {
            val settings = repository.observe().first()
            assertEquals(ThemeMode.System, settings.themeMode)
        }

    @Test
    fun `default language is null (follow system locale)`() =
        runTest {
            val settings = repository.observe().first()
            assertNull(settings.languageTag)
        }

    @Test
    fun `default dynamicColor is true on Android 12+ semantics (opt-in by default)`() =
        runTest {
            val settings = repository.observe().first()
            assertTrue(settings.dynamicColor, "dynamicColor must default to true so Material You is opt-in")
        }

    @Test
    fun `default calculation method is EPIGENETIC (Wang formula)`() =
        runTest {
            // ADR-0006: Wang et al. formula — default для собак.
            val settings = repository.observe().first()
            assertEquals(CalculationMethod.EPIGENETIC, settings.defaultCalculationMethod)
        }

    @Test
    fun `setting theme mode to Dark persists across observe() reads`() =
        runTest {
            repository.setThemeMode(ThemeMode.Dark)
            val settings = repository.observe().first()
            assertEquals(ThemeMode.Dark, settings.themeMode)
        }

    @Test
    fun `setting theme mode to Light persists across observe() reads`() =
        runTest {
            repository.setThemeMode(ThemeMode.Light)
            val settings = repository.observe().first()
            assertEquals(ThemeMode.Light, settings.themeMode)
        }

    @Test
    fun `setting language tag to ru persists`() =
        runTest {
            repository.setLanguageTag("ru")
            val settings = repository.observe().first()
            assertEquals("ru", settings.languageTag)
        }

    @Test
    fun `setting language tag to null clears stored value (revert to system)`() =
        runTest {
            repository.setLanguageTag("en")
            repository.setLanguageTag(null)
            val settings = repository.observe().first()
            assertNull(settings.languageTag)
        }

    @Test
    fun `toggling dynamicColor to false persists`() =
        runTest {
            repository.setDynamicColor(false)
            val settings = repository.observe().first()
            assertFalse(settings.dynamicColor)
        }

    @Test
    fun `setting default calculation method to SIZE_BASED persists`() =
        runTest {
            repository.setDefaultCalculationMethod(CalculationMethod.SIZE_BASED)
            val settings = repository.observe().first()
            assertEquals(CalculationMethod.SIZE_BASED, settings.defaultCalculationMethod)
        }

    @Test
    fun `observe() emits new AppSettings snapshot on each write (Turbine)`() =
        runTest {
            repository.observe().test {
                // Первая эмиссия — defaults.
                val first = awaitItem()
                assertEquals(ThemeMode.System, first.themeMode)
                assertEquals(CalculationMethod.EPIGENETIC, first.defaultCalculationMethod)

                // После записи Dark — новая эмиссия с обновлённым themeMode.
                repository.setThemeMode(ThemeMode.Dark)
                val second = awaitItem()
                assertEquals(ThemeMode.Dark, second.themeMode)
                // Остальные поля должны остаться нетронутыми.
                assertEquals(CalculationMethod.EPIGENETIC, second.defaultCalculationMethod)

                // После записи SIZE_BASED — ещё одна эмиссия, themeMode сохраняется как Dark.
                repository.setDefaultCalculationMethod(CalculationMethod.SIZE_BASED)
                val third = awaitItem()
                assertEquals(ThemeMode.Dark, third.themeMode)
                assertEquals(CalculationMethod.SIZE_BASED, third.defaultCalculationMethod)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `corrupted theme mode id in storage falls back to System default`() =
        runTest {
            // Прямая запись повреждённого id в DataStore — имитируем сценарий,
            // когда пользователь обновил приложение и enum-константа была переименована.
            dataStore.edit { prefs ->
                prefs[SettingsKeys.THEME_MODE] = "neon_punk_2077_unknown"
            }
            val settings = repository.observe().first()
            assertEquals(ThemeMode.System, settings.themeMode, "Corrupted id must fall back to default")
        }

    @Test
    fun `corrupted calculation method name in storage falls back to EPIGENETIC default`() =
        runTest {
            dataStore.edit { prefs ->
                prefs[SettingsKeys.DEFAULT_CALCULATION_METHOD] = "TIME_DILATION_FORMULA_V9000"
            }
            val settings = repository.observe().first()
            assertEquals(CalculationMethod.EPIGENETIC, settings.defaultCalculationMethod)
        }

    @Test
    fun `repository can be re-created from same DataStore and reads persisted state`() =
        runTest {
            repository.setThemeMode(ThemeMode.Dark)
            repository.setLanguageTag("en")
            repository.setDynamicColor(false)
            repository.setDefaultCalculationMethod(CalculationMethod.SIZE_BASED)

            // Новый экземпляр репозитория поверх того же DataStore — должен видеть всё что записал старый.
            val anotherRepository = SettingsRepositoryImpl(dataStore)
            val settings = anotherRepository.observe().first()

            assertNotNull(settings)
            assertEquals(ThemeMode.Dark, settings.themeMode)
            assertEquals("en", settings.languageTag)
            assertFalse(settings.dynamicColor)
            assertEquals(CalculationMethod.SIZE_BASED, settings.defaultCalculationMethod)
        }
}
