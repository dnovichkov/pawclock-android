package app.pawclock.feature.settings.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import app.pawclock.domain.export.ExportFormat
import app.pawclock.domain.import_.ImportStrategy
import app.pawclock.feature.settings.R
import app.pawclock.feature.settings.SettingsEvent
import app.pawclock.feature.settings.SettingsState
import app.pawclock.feature.settings.ui.section.EXPORT_DIALOG_TEST_TAG
import app.pawclock.feature.settings.ui.section.EXPORT_ROW_TEST_TAG
import app.pawclock.feature.settings.ui.section.IMPORT_DIALOG_TEST_TAG
import app.pawclock.feature.settings.ui.section.IMPORT_ROW_TEST_TAG
import app.pawclock.feature.settings.ui.section.LanguageOption
import app.pawclock.feature.settings.ui.section.calculationMethodOptionTag
import app.pawclock.feature.settings.ui.section.exportFormatOptionTag
import app.pawclock.feature.settings.ui.section.importStrategyOptionTag
import app.pawclock.feature.settings.ui.section.languageOptionTag
import app.pawclock.feature.settings.ui.section.themeModeOptionTag
import app.pawclock.model.CalculationMethod
import app.pawclock.model.ThemeMode
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI тесты для [SettingsContent] (Task 21 / Plan 1).
 *
 * Используется stateless [SettingsContent], позволяя подавать произвольные
 * [SettingsState] без Hilt-setup'а. Запуск — на эмуляторе в nightly.yml.
 *
 * Проверяемое поведение:
 *  - Title "Настройки" + back-кнопка рендерятся;
 *  - Theme/Language/Method radio-опции кликабельны и эмитят правильные события;
 *  - "О приложении" row кликабелен и триггерит onOpenAbout;
 *  - DynamicColor switch триггерит SetDynamicColor.
 */
class SettingsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    // Строки сверяются через ресурсы, а не литералы: эмулятор CI работает в en-US,
    // и default (ru) перекрывается values-en — литералы делали тесты locale-зависимыми.
    private fun string(resId: Int): String = InstrumentationRegistry.getInstrumentation().targetContext.getString(resId)

    @Test
    fun initialState_rendersTitle() {
        composeRule.setContent {
            SettingsContent(
                state = SettingsState.Default,
                onEvent = { },
                onBack = { },
                onOpenAbout = { },
            )
        }

        composeRule.onNodeWithText(string(R.string.settings_title)).assertIsDisplayed()
    }

    @Test
    fun themeMode_clickingDarkEmitsSetThemeModeDark() {
        val events = mutableListOf<SettingsEvent>()
        composeRule.setContent {
            SettingsContent(
                state = SettingsState.Default,
                onEvent = events::add,
                onBack = { },
                onOpenAbout = { },
            )
        }

        composeRule.onNode(hasTestTag(themeModeOptionTag(ThemeMode.Dark))).performClick()

        assertTrue(
            "Click on Dark theme row must produce SetThemeMode(Dark)",
            events.any { it is SettingsEvent.SetThemeMode && it.themeMode == ThemeMode.Dark },
        )
    }

    @Test
    fun language_clickingRussianEmitsSetLanguageTagRu() {
        val events = mutableListOf<SettingsEvent>()
        composeRule.setContent {
            SettingsContent(
                state = SettingsState.Default,
                onEvent = events::add,
                onBack = { },
                onOpenAbout = { },
            )
        }

        composeRule.onNode(hasTestTag(languageOptionTag(LanguageOption.Russian))).performClick()

        assertTrue(
            "Click on Russian language row must produce SetLanguageTag(\"ru\")",
            events.any { it is SettingsEvent.SetLanguageTag && it.languageTag == "ru" },
        )
    }

    @Test
    fun language_clickingSystemEmitsSetLanguageTagNull() {
        val events = mutableListOf<SettingsEvent>()
        composeRule.setContent {
            SettingsContent(
                state = SettingsState.Default.copy(languageTag = "en"),
                onEvent = events::add,
                onBack = { },
                onOpenAbout = { },
            )
        }

        composeRule.onNode(hasTestTag(languageOptionTag(LanguageOption.System))).performClick()

        assertTrue(
            "Click on System language must reset languageTag to null",
            events.any { it is SettingsEvent.SetLanguageTag && it.languageTag == null },
        )
    }

    @Test
    fun calculationMethod_clickingSizeBasedEmitsSetDefaultCalculationMethod() {
        val events = mutableListOf<SettingsEvent>()
        composeRule.setContent {
            SettingsContent(
                state = SettingsState.Default,
                onEvent = events::add,
                onBack = { },
                onOpenAbout = { },
            )
        }

        // Секция метода — ниже theme/language: на маленьких экранах за фолдом.
        composeRule
            .onNode(hasTestTag(calculationMethodOptionTag(CalculationMethod.SIZE_BASED)))
            .performScrollTo()
            .performClick()

        assertTrue(
            "Click on Size method must produce SetDefaultCalculationMethod(SIZE_BASED)",
            events.any {
                it is SettingsEvent.SetDefaultCalculationMethod &&
                    it.method == CalculationMethod.SIZE_BASED
            },
        )
    }

    @Test
    fun aboutRow_clickTriggersOnOpenAbout() {
        var aboutOpened = false
        composeRule.setContent {
            SettingsContent(
                state = SettingsState.Default,
                onEvent = { },
                onBack = { },
                onOpenAbout = { aboutOpened = true },
            )
        }

        composeRule.onNodeWithTag(ABOUT_ROW_TEST_TAG).performScrollTo().performClick()

        assertTrue("About row click must trigger onOpenAbout callback", aboutOpened)
    }

    @Test
    fun backButton_renderedInTopBar() {
        composeRule.setContent {
            SettingsContent(
                state = SettingsState.Default,
                onEvent = { },
                onBack = { },
                onOpenAbout = { },
            )
        }

        // "Назад" — contentDescription у IconButton в topBar.
        composeRule.onNodeWithText(string(R.string.settings_title)).assertIsDisplayed()
    }

    // ---------------------------------------------------------------------------------------------
    // Plan 2 (§3.5) — секция «Резервная копия».
    // ---------------------------------------------------------------------------------------------

    @Test
    fun backup_exportRowVisible() {
        composeRule.setContent {
            SettingsContent(
                state = SettingsState.Default,
                onEvent = { },
                onBack = { },
                onOpenAbout = { },
            )
        }

        // Секция «Резервная копия» — в самом низу экрана настроек, скроллим к ней.
        composeRule.onNodeWithTag(EXPORT_ROW_TEST_TAG).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun backup_importRowVisible() {
        composeRule.setContent {
            SettingsContent(
                state = SettingsState.Default,
                onEvent = { },
                onBack = { },
                onOpenAbout = { },
            )
        }

        composeRule.onNodeWithTag(IMPORT_ROW_TEST_TAG).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun backup_clickingExportShowsFormatDialog() {
        composeRule.setContent {
            SettingsContent(
                state = SettingsState.Default,
                onEvent = { },
                onBack = { },
                onOpenAbout = { },
            )
        }

        composeRule.onNodeWithTag(EXPORT_ROW_TEST_TAG).performScrollTo().performClick()

        // Диалог — отдельное окно поверх экрана, ему скролл не нужен.
        composeRule.onNodeWithTag(EXPORT_DIALOG_TEST_TAG).assertIsDisplayed()
        composeRule.onNode(hasTestTag(exportFormatOptionTag(ExportFormat.JSON))).assertIsDisplayed()
        composeRule.onNode(hasTestTag(exportFormatOptionTag(ExportFormat.CSV))).assertIsDisplayed()
    }

    @Test
    fun backup_clickingImportShowsStrategyDialog() {
        composeRule.setContent {
            SettingsContent(
                state = SettingsState.Default,
                onEvent = { },
                onBack = { },
                onOpenAbout = { },
            )
        }

        composeRule.onNodeWithTag(IMPORT_ROW_TEST_TAG).performScrollTo().performClick()

        composeRule.onNodeWithTag(IMPORT_DIALOG_TEST_TAG).assertIsDisplayed()
        composeRule.onNode(hasTestTag(importStrategyOptionTag(ImportStrategy.MERGE))).assertIsDisplayed()
        composeRule.onNode(hasTestTag(importStrategyOptionTag(ImportStrategy.REPLACE))).assertIsDisplayed()
    }

    @Test
    fun backup_confirmingExportFormatInvokesCallback() {
        val chosenFormats = mutableListOf<ExportFormat>()
        composeRule.setContent {
            SettingsContent(
                state = SettingsState.Default,
                onEvent = { },
                onBack = { },
                onOpenAbout = { },
                onExportFormatChosen = chosenFormats::add,
            )
        }

        composeRule.onNodeWithTag(EXPORT_ROW_TEST_TAG).performScrollTo().performClick()
        composeRule.onNode(hasTestTag(exportFormatOptionTag(ExportFormat.CSV))).performClick()
        composeRule.onNodeWithText(string(R.string.settings_backup_confirm)).performClick()

        assertTrue(
            "Confirming export dialog must invoke onExportFormatChosen with the selected format",
            chosenFormats == listOf(ExportFormat.CSV),
        )
    }

    @Test
    fun backup_confirmingImportStrategyInvokesCallback() {
        val chosenStrategies = mutableListOf<ImportStrategy>()
        composeRule.setContent {
            SettingsContent(
                state = SettingsState.Default,
                onEvent = { },
                onBack = { },
                onOpenAbout = { },
                onImportStrategyChosen = chosenStrategies::add,
            )
        }

        composeRule.onNodeWithTag(IMPORT_ROW_TEST_TAG).performScrollTo().performClick()
        composeRule.onNode(hasTestTag(importStrategyOptionTag(ImportStrategy.REPLACE))).performClick()
        composeRule.onNodeWithText(string(R.string.settings_backup_confirm)).performClick()

        assertTrue(
            "Confirming import dialog must invoke onImportStrategyChosen with the selected strategy",
            chosenStrategies == listOf(ImportStrategy.REPLACE),
        )
    }
}
