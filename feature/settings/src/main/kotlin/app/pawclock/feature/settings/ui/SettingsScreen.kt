package app.pawclock.feature.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pawclock.feature.settings.SettingsEvent
import app.pawclock.feature.settings.SettingsState
import app.pawclock.feature.settings.SettingsViewModel
import app.pawclock.feature.settings.ui.section.CalculationMethodSelector
import app.pawclock.feature.settings.ui.section.DynamicColorSwitch
import app.pawclock.feature.settings.ui.section.LanguageSelector
import app.pawclock.feature.settings.ui.section.SettingsSectionHeader
import app.pawclock.feature.settings.ui.section.ThemeModeSelector

/**
 * Экран настроек приложения (§5.3, Task 21 / Plan 1).
 *
 * Структура — иерархия секций (тема / язык / расчёт / о приложении), каждая
 * со своим заголовком. Это match'ит Material 3 preferences-pattern и облегчает
 * визуальный поиск нужной настройки.
 *
 * Кнопка "О приложении" навигирует на отдельный AboutScreen (§5.3); ViewModel
 * не получает обратную связь о навигации (callback-driven, как в QuickCalcScreen).
 *
 * Локализация: hardcoded русские строки на этом этапе; полный stringResource — Task 22.
 *
 * @param onBack toolbar-back / системная кнопка возврата.
 * @param onOpenAbout навигация на AboutScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsContent(
        state = state,
        onEvent = viewModel::handleEvent,
        onBack = onBack,
        onOpenAbout = onOpenAbout,
        modifier = modifier,
    )
}

/**
 * Stateless вариант экрана для testability — Compose-тесты подают [SettingsState]
 * напрямую без Hilt-setup'а.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsContent(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    onBack: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Настройки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                        )
                    }
                },
            )
        },
    ) { padding ->
        SettingsBody(
            state = state,
            onEvent = onEvent,
            onOpenAbout = onOpenAbout,
            padding = padding,
        )
    }
}

@Composable
private fun SettingsBody(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    onOpenAbout: () -> Unit,
    padding: PaddingValues,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        SettingsSectionHeader(text = "Внешний вид")
        ThemeModeSelector(
            selected = state.themeMode,
            onSelect = { onEvent(SettingsEvent.SetThemeMode(it)) },
        )
        DynamicColorSwitch(
            enabled = state.dynamicColor,
            onToggle = { onEvent(SettingsEvent.SetDynamicColor(enabled = it)) },
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = DIVIDER_PADDING_DP.dp))

        SettingsSectionHeader(text = "Язык")
        LanguageSelector(
            selected = state.languageTag,
            onSelect = { onEvent(SettingsEvent.SetLanguageTag(it)) },
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = DIVIDER_PADDING_DP.dp))

        SettingsSectionHeader(text = "Расчёт возраста собак")
        CalculationMethodSelector(
            selected = state.defaultCalculationMethod,
            onSelect = { onEvent(SettingsEvent.SetDefaultCalculationMethod(it)) },
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = DIVIDER_PADDING_DP.dp))

        ListItem(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenAbout)
                    .testTag(ABOUT_ROW_TEST_TAG),
            headlineContent = { Text(text = "О приложении") },
            supportingContent = { Text(text = "Версия, лицензия, источники") },
            trailingContent = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                )
            },
        )
    }
}

internal const val ABOUT_ROW_TEST_TAG: String = "settings_about_row"

private const val DIVIDER_PADDING_DP: Int = 4
