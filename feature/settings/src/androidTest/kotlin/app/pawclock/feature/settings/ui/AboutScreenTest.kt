package app.pawclock.feature.settings.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI тесты для [AboutScreen] (Task 21 / Plan 1).
 *
 * Проверяемое поведение:
 *  - title "О приложении" рендерится;
 *  - переданная версия отображается;
 *  - Apache 2.0 лицензия видна;
 *  - дисклеймер из §3.3 присутствует;
 *  - ссылка на GitHub repo видна;
 *  - все ключевые научные источники (Wang DOI, AAFP DOI) отображаются.
 */
class AboutScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersAppTitle() {
        composeRule.setContent {
            AboutScreen(appVersion = "0.1.0", onBack = { })
        }

        val matches = composeRule.onAllNodesWithText("PawClock").fetchSemanticsNodes()
        assertTrue("At least one PawClock title must be rendered", matches.isNotEmpty())
    }

    @Test
    fun rendersPassedAppVersion() {
        composeRule.setContent {
            AboutScreen(appVersion = "1.2.3-test", onBack = { })
        }

        composeRule.onNodeWithTag(VERSION_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithText("Версия 1.2.3-test").assertIsDisplayed()
    }

    @Test
    fun rendersApacheLicenseLabel() {
        composeRule.setContent {
            AboutScreen(appVersion = "0.1.0", onBack = { })
        }

        composeRule.onNodeWithText("Apache License 2.0").assertIsDisplayed()
    }

    @Test
    fun rendersGithubRepositoryLink() {
        composeRule.setContent {
            AboutScreen(appVersion = "0.1.0", onBack = { })
        }

        composeRule.onNodeWithTag(REPOSITORY_LINK_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithText("github.com/dnovichkov/pawclock-android").assertIsDisplayed()
    }

    @Test
    fun rendersDisclaimer() {
        composeRule.setContent {
            AboutScreen(appVersion = "0.1.0", onBack = { })
        }

        composeRule.onNodeWithTag(DISCLAIMER_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun rendersScientificSourcesHeader() {
        composeRule.setContent {
            AboutScreen(appVersion = "0.1.0", onBack = { })
        }

        composeRule.onNodeWithTag(SOURCES_HEADER_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun rendersWang2020DOI() {
        composeRule.setContent {
            AboutScreen(appVersion = "0.1.0", onBack = { })
        }

        // Wang Cell Systems 2020 DOI — критично для educational angle.
        composeRule.onNodeWithText("DOI: 10.1016/j.cels.2020.06.006").assertIsDisplayed()
    }

    @Test
    fun rendersAAFP2021DOI() {
        composeRule.setContent {
            AboutScreen(appVersion = "0.1.0", onBack = { })
        }

        composeRule.onNodeWithText("DOI: 10.1177/1098612X21993657").assertIsDisplayed()
    }

    @Test
    fun backButton_clickTriggersOnBack() {
        var backCalled = false
        composeRule.setContent {
            AboutScreen(appVersion = "0.1.0", onBack = { backCalled = true })
        }

        // "Назад" — это contentDescription у IconButton в TopAppBar.
        composeRule.onNodeWithContentDescription("Назад").performClick()

        assertTrue("Back button click must trigger onBack callback", backCalled)
    }
}
