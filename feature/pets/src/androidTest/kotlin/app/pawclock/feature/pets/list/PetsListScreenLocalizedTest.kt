package app.pawclock.feature.pets.list

import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import app.pawclock.feature.pets.list.ui.PetsListContent
import java.util.Locale
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI тесты локализации PetsListScreen (Task 22).
 *
 * Стратегия: оборачиваем тестовый Composable в [CompositionLocalProvider] с
 * переопределённой [LocalConfiguration.locales] — Compose resolver выбирает
 * `values-en/strings.xml` ресурсы вместо default `values/strings.xml`.
 *
 * Тестируем:
 *  - ru (default локаль) — корректные русские строки;
 *  - en — корректные английские переводы.
 *
 * Это не тестирует реальное переключение через `AppCompatDelegate.setApplicationLocales`
 * (для этого нужен полноценный Activity recreate в инструментальном тесте). Цель —
 * убедиться, что обе локали имеют все ключи и нет «холостых» fallback'ов.
 *
 * Запускается как androidTest на эмуляторе в nightly.yml.
 */
class PetsListScreenLocalizedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun russian_locale_shows_russian_empty_state() {
        composeRule.setContent {
            WithLocale(Locale("ru")) {
                PetsListContent(
                    state = PetsListState.Empty,
                    onPetClick = {},
                    onAddPetClick = {},
                )
            }
        }
        composeRule.onNodeWithText("Добавьте первого питомца").assertIsDisplayed()
        composeRule.onNodeWithText("Питомцы").assertIsDisplayed()
        composeRule.onNodeWithText("Добавить").assertIsDisplayed()
    }

    @Test
    fun english_locale_shows_english_empty_state() {
        composeRule.setContent {
            WithLocale(Locale("en")) {
                PetsListContent(
                    state = PetsListState.Empty,
                    onPetClick = {},
                    onAddPetClick = {},
                )
            }
        }
        composeRule.onNodeWithText("Add your first pet").assertIsDisplayed()
        composeRule.onNodeWithText("Pets").assertIsDisplayed()
        composeRule.onNodeWithText("Add").assertIsDisplayed()
    }

    @androidx.compose.runtime.Composable
    private fun WithLocale(
        locale: Locale,
        content: @androidx.compose.runtime.Composable () -> Unit,
    ) {
        val context = LocalContext.current
        val configuration =
            Configuration(context.resources.configuration).apply {
                setLocale(locale)
            }
        val localizedContext = context.createConfigurationContext(configuration)
        CompositionLocalProvider(
            LocalContext provides localizedContext,
            LocalConfiguration provides configuration,
            content = content,
        )
    }
}
