package app.pawclock.feature.settings.ui.section

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.pawclock.domain.export.ExportFormat
import app.pawclock.domain.import_.ImportStrategy
import app.pawclock.feature.settings.R

/**
 * Секция «Резервная копия» (§3.5, Plan 2 Task 21) — две строки действий: экспорт и импорт.
 *
 * Управляет локальным UI-состоянием двух диалогов выбора:
 *  - экспорт → диалог формата (JSON / CSV) → [onExportFormatChosen];
 *  - импорт → диалог стратегии (Merge / Replace) → [onImportStrategyChosen].
 *
 * Сам выбор файла (SAF) и показ результата — ответственность stateful-обёртки
 * [app.pawclock.feature.settings.ui.SettingsScreen], которая реагирует на эффекты ViewModel.
 *
 * @param onExportFormatChosen вызывается с выбранным форматом после подтверждения диалога экспорта.
 * @param onImportStrategyChosen вызывается с выбранной стратегией после подтверждения диалога импорта.
 */
@Composable
internal fun BackupSection(
    onExportFormatChosen: (ExportFormat) -> Unit,
    onImportStrategyChosen: (ImportStrategy) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        ListItem(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { showExportDialog = true }
                    .testTag(EXPORT_ROW_TEST_TAG),
            headlineContent = { Text(text = stringResource(R.string.settings_export_title)) },
            supportingContent = { Text(text = stringResource(R.string.settings_export_supporting)) },
        )
        ListItem(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { showImportDialog = true }
                    .testTag(IMPORT_ROW_TEST_TAG),
            headlineContent = { Text(text = stringResource(R.string.settings_import_title)) },
            supportingContent = { Text(text = stringResource(R.string.settings_import_supporting)) },
        )
    }

    if (showExportDialog) {
        ExportFormatDialog(
            onDismiss = { showExportDialog = false },
            onConfirm = { format ->
                showExportDialog = false
                onExportFormatChosen(format)
            },
        )
    }

    if (showImportDialog) {
        ImportStrategyDialog(
            onDismiss = { showImportDialog = false },
            onConfirm = { strategy ->
                showImportDialog = false
                onImportStrategyChosen(strategy)
            },
        )
    }
}

@Composable
private fun ExportFormatDialog(
    onDismiss: () -> Unit,
    onConfirm: (ExportFormat) -> Unit,
) {
    var selected by remember { mutableStateOf(ExportFormat.JSON) }
    AlertDialog(
        modifier = Modifier.testTag(EXPORT_DIALOG_TEST_TAG),
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.settings_export_format_dialog_title)) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                RadioOptionRow(
                    label = stringResource(R.string.settings_export_format_json),
                    selected = selected == ExportFormat.JSON,
                    testTag = exportFormatOptionTag(ExportFormat.JSON),
                    onSelect = { selected = ExportFormat.JSON },
                )
                RadioOptionRow(
                    label = stringResource(R.string.settings_export_format_csv),
                    selected = selected == ExportFormat.CSV,
                    testTag = exportFormatOptionTag(ExportFormat.CSV),
                    onSelect = { selected = ExportFormat.CSV },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) {
                Text(text = stringResource(R.string.settings_backup_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.settings_backup_cancel))
            }
        },
    )
}

@Composable
private fun ImportStrategyDialog(
    onDismiss: () -> Unit,
    onConfirm: (ImportStrategy) -> Unit,
) {
    var selected by remember { mutableStateOf(ImportStrategy.MERGE) }
    AlertDialog(
        modifier = Modifier.testTag(IMPORT_DIALOG_TEST_TAG),
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.settings_import_strategy_dialog_title)) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                RadioOptionRow(
                    label = stringResource(R.string.settings_import_strategy_merge),
                    selected = selected == ImportStrategy.MERGE,
                    testTag = importStrategyOptionTag(ImportStrategy.MERGE),
                    onSelect = { selected = ImportStrategy.MERGE },
                )
                RadioOptionRow(
                    label = stringResource(R.string.settings_import_strategy_replace),
                    selected = selected == ImportStrategy.REPLACE,
                    testTag = importStrategyOptionTag(ImportStrategy.REPLACE),
                    onSelect = { selected = ImportStrategy.REPLACE },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) {
                Text(text = stringResource(R.string.settings_backup_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.settings_backup_cancel))
            }
        },
    )
}

@Composable
private fun RadioOptionRow(
    label: String,
    selected: Boolean,
    testTag: String,
    onSelect: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton)
                .padding(vertical = ROW_VERTICAL_PADDING_DP.dp)
                .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ROW_GAP_DP.dp),
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

internal fun exportFormatOptionTag(format: ExportFormat): String = "settings_export_format_${format.name.lowercase()}"

internal fun importStrategyOptionTag(strategy: ImportStrategy): String =
    "settings_import_strategy_${strategy.name.lowercase()}"

internal const val EXPORT_ROW_TEST_TAG: String = "settings_export_row"
internal const val IMPORT_ROW_TEST_TAG: String = "settings_import_row"
internal const val EXPORT_DIALOG_TEST_TAG: String = "settings_export_dialog"
internal const val IMPORT_DIALOG_TEST_TAG: String = "settings_import_dialog"

private const val ROW_VERTICAL_PADDING_DP: Int = 8
private const val ROW_GAP_DP: Int = 12
