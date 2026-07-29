package app.pawclock.feature.settings.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pawclock.domain.export.ExportFormat
import app.pawclock.domain.import_.ImportStrategy
import app.pawclock.feature.settings.R
import app.pawclock.feature.settings.SettingsEffect
import app.pawclock.feature.settings.SettingsEvent
import app.pawclock.feature.settings.SettingsMessages
import app.pawclock.feature.settings.SettingsState
import app.pawclock.feature.settings.SettingsViewModel
import app.pawclock.feature.settings.ui.section.BackupSection
import app.pawclock.feature.settings.ui.section.CalculationMethodSelector
import app.pawclock.feature.settings.ui.section.DynamicColorSwitch
import app.pawclock.feature.settings.ui.section.LanguageSelector
import app.pawclock.feature.settings.ui.section.SettingsSectionHeader
import app.pawclock.feature.settings.ui.section.ThemeModeSelector
import kotlinx.coroutines.launch

/**
 * Экран настроек приложения (§5.3, Task 21 / Plan 1 + Plan 2 Task 21).
 *
 * Структура — иерархия секций (тема / язык / расчёт / резервная копия / о приложении).
 *
 * Plan 2 (§3.5): секция «Резервная копия». Stateful-обёртка коллектит one-time
 * [SettingsViewModel.effects]: на запрос location'а запускает SAF-launcher
 * (`ACTION_CREATE_DOCUMENT` / `ACTION_OPEN_DOCUMENT`), на результат — показывает Snackbar.
 * Выбранная стратегия импорта запоминается между [SettingsEvent.ImportRequested] и
 * приходом `Uri` (SAF-диалог теряет контекст), затем уходит в [SettingsEvent.ImportLocationSelected].
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
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Стратегия импорта, выбранная в диалоге, нужна на шаге ImportLocationSelected —
    // SAF-диалог между шагами теряет UI-контекст, поэтому держим её здесь.
    var pendingImportStrategy by remember { mutableStateOf(ImportStrategy.MERGE) }

    val createJsonLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(MIME_JSON)) { uri ->
            uri?.let { viewModel.handleEvent(SettingsEvent.ExportLocationSelected(it.toString())) }
        }
    val createCsvLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(MIME_CSV)) { uri ->
            uri?.let { viewModel.handleEvent(SettingsEvent.ExportLocationSelected(it.toString())) }
        }
    val openLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                viewModel.handleEvent(SettingsEvent.ImportLocationSelected(it.toString(), pendingImportStrategy))
            }
        }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SettingsEffect.RequestSaveLocation ->
                    if (effect.mimeType == MIME_CSV) {
                        createCsvLauncher.launch(effect.suggestedFilename)
                    } else {
                        createJsonLauncher.launch(effect.suggestedFilename)
                    }
                is SettingsEffect.RequestOpenLocation ->
                    openLauncher.launch(effect.mimeTypes.toTypedArray())
                is SettingsEffect.ExportComplete ->
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            context.getString(R.string.settings_export_success, effect.petCount),
                        )
                    }
                is SettingsEffect.ImportComplete ->
                    scope.launch { snackbarHostState.showSnackbar(importCompleteMessage(context, effect)) }
                is SettingsEffect.ExportError ->
                    scope.launch {
                        snackbarHostState.showSnackbar(context.getString(backupErrorResId(effect.messageKey)))
                    }
                is SettingsEffect.ImportError ->
                    scope.launch {
                        snackbarHostState.showSnackbar(context.getString(backupErrorResId(effect.messageKey)))
                    }
            }
        }
    }

    SettingsContent(
        state = state,
        onEvent = viewModel::handleEvent,
        onBack = onBack,
        onOpenAbout = onOpenAbout,
        onExportFormatChosen = { format -> viewModel.handleEvent(SettingsEvent.ExportRequested(format)) },
        onImportStrategyChosen = { strategy ->
            pendingImportStrategy = strategy
            viewModel.handleEvent(SettingsEvent.ImportRequested)
        },
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

/**
 * Stateless вариант экрана для testability — Compose-тесты подают [SettingsState]
 * напрямую без Hilt-setup'а. Export/import callbacks по умолчанию no-op, чтобы
 * существующие тесты могли конструировать экран без SAF-обвязки.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsContent(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    onBack: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier,
    onExportFormatChosen: (ExportFormat) -> Unit = {},
    onImportStrategyChosen: (ImportStrategy) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back),
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
            onExportFormatChosen = onExportFormatChosen,
            onImportStrategyChosen = onImportStrategyChosen,
            padding = padding,
        )
    }
}

@Composable
private fun SettingsBody(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    onOpenAbout: () -> Unit,
    onExportFormatChosen: (ExportFormat) -> Unit,
    onImportStrategyChosen: (ImportStrategy) -> Unit,
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
        SettingsSectionHeader(text = stringResource(R.string.settings_section_appearance))
        ThemeModeSelector(
            selected = state.themeMode,
            onSelect = { onEvent(SettingsEvent.SetThemeMode(it)) },
        )
        DynamicColorSwitch(
            enabled = state.dynamicColor,
            onToggle = { onEvent(SettingsEvent.SetDynamicColor(enabled = it)) },
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = DIVIDER_PADDING_DP.dp))

        SettingsSectionHeader(text = stringResource(R.string.settings_section_language))
        LanguageSelector(
            selected = state.languageTag,
            onSelect = { onEvent(SettingsEvent.SetLanguageTag(it)) },
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = DIVIDER_PADDING_DP.dp))

        SettingsSectionHeader(text = stringResource(R.string.settings_section_calculation))
        CalculationMethodSelector(
            selected = state.defaultCalculationMethod,
            onSelect = { onEvent(SettingsEvent.SetDefaultCalculationMethod(it)) },
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = DIVIDER_PADDING_DP.dp))

        SettingsSectionHeader(text = stringResource(R.string.settings_section_backup))
        BackupSection(
            onExportFormatChosen = onExportFormatChosen,
            onImportStrategyChosen = onImportStrategyChosen,
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = DIVIDER_PADDING_DP.dp))

        ListItem(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenAbout)
                    .testTag(ABOUT_ROW_TEST_TAG),
            headlineContent = { Text(text = stringResource(R.string.settings_open_about_title)) },
            supportingContent = { Text(text = stringResource(R.string.settings_open_about_supporting)) },
            trailingContent = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                )
            },
        )
    }
}

/** Локализованное сообщение об успешном импорте — с учётом наличия предупреждений. */
private fun importCompleteMessage(
    context: android.content.Context,
    effect: SettingsEffect.ImportComplete,
): String =
    if (effect.warnings.isEmpty()) {
        context.getString(R.string.settings_import_success, effect.petCount)
    } else {
        context.getString(
            R.string.settings_import_success_with_warnings,
            effect.petCount,
            effect.warnings.size,
        )
    }

/** Маппит стабильный ключ ошибки export/import на строковый ресурс. */
@androidx.annotation.StringRes
private fun backupErrorResId(messageKey: String): Int =
    when (messageKey) {
        SettingsMessages.IMPORT_ERROR_MALFORMED -> R.string.settings_import_error_malformed
        SettingsMessages.IMPORT_ERROR_MISSING_FIELD -> R.string.settings_import_error_missing_field
        SettingsMessages.IMPORT_ERROR_UNKNOWN_SPECIES -> R.string.settings_import_error_unknown_species
        SettingsMessages.IMPORT_ERROR_UNSUPPORTED_VERSION -> R.string.settings_import_error_unsupported_version
        SettingsMessages.IMPORT_ERROR_READ_FAILED -> R.string.settings_import_error_read_failed
        else -> R.string.settings_export_error_write_failed
    }

internal const val ABOUT_ROW_TEST_TAG: String = "settings_about_row"

private const val MIME_JSON: String = "application/json"
private const val MIME_CSV: String = "text/csv"
private const val DIVIDER_PADDING_DP: Int = 4
