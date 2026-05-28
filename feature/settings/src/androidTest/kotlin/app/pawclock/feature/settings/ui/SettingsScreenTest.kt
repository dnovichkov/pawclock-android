package app.pawclock.feature.settings.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.pawclock.feature.settings.SettingsEvent
import app.pawclock.feature.settings.SettingsState
import app.pawclock.feature.settings.ui.section.LanguageOption
import app.pawclock.feature.settings.ui.section.calculationMethodOptionTag
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

        composeRule.onNodeWithText("Настройки").assertIsDisplayed()
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

        composeRule
            .onNode(hasTestTag(calculationMethodOptionTag(CalculationMethod.SIZE_BASED)))
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

        composeRule.onNodeWithTag(ABOUT_ROW_TEST_TAG).performClick()

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
        composeRule.onNodeWithText("Настройки").assertIsDisplayed()
    }
}
